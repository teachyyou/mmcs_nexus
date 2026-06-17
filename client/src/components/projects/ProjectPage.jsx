import React, { useEffect, useState } from 'react';
import {
    Alert,
    Box,
    Button,
    Chip,
    CircularProgress,
    Container,
    Paper,
    Stack,
    Typography,
} from '@mui/material';
import ArrowBackIosNewIcon from '@mui/icons-material/ArrowBackIosNew';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../../AuthContext';
import ProjectSubmissionsForm from './ProjectSubmissionsForm';

const ProjectPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { captainProject, setCaptainProject } = useAuth();

    const [project, setProject] = useState(null);
    const [loading, setLoading] = useState(true);
    const [claiming, setClaiming] = useState(false);
    const [error, setError] = useState(null);
    const [successMessage, setSuccessMessage] = useState(null);

    const loadProject = async () => {
        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`/api/v1/user/projects/${id}`, {
                credentials: 'include',
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || 'Проект не найден');
            }

            setProject(data);
        } catch (requestError) {
            setError(requestError.message || 'Ошибка загрузки проекта');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadProject();
    }, [id]);

    const claimCaptain = async () => {
        setClaiming(true);
        setError(null);
        setSuccessMessage(null);

        try {
            const response = await fetch(`/api/v1/user/projects/${id}/claim-captain`, {
                method: 'POST',
                credentials: 'include',
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || 'Не удалось стать капитаном проекта');
            }

            setProject(data.project);
            setCaptainProject({
                id: data.project.id,
                name: data.project.name,
            });
            setSuccessMessage(`Вы стали капитаном проекта «${data.project.name}»`);
        } catch (requestError) {
            setError(requestError.message || 'Ошибка привязки проекта');
        } finally {
            setClaiming(false);
        }
    };

    const hasMyProject = Boolean(captainProject);

    return (
        <Box sx={{ pt: 10, pb: 6, minHeight: '100vh' }}>
            <Container maxWidth="md">
                <Button
                    startIcon={<ArrowBackIosNewIcon />}
                    onClick={() => navigate(-1)}
                    sx={{ mb: 3 }}
                >
                    Назад
                </Button>

                {loading && (
                    <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
                        <CircularProgress />
                    </Box>
                )}

                {error && (
                    <Alert severity="error" sx={{ mb: 3 }}>
                        {error}
                    </Alert>
                )}

                {successMessage && (
                    <Alert severity="success" sx={{ mb: 3 }}>
                        {successMessage}
                    </Alert>
                )}

                {!loading && !error && project && (
                    <>
                        <Paper sx={{ borderRadius: 3, overflow: 'hidden' }}>
                            <Box sx={{ p: { xs: 3, sm: 4 } }}>
                                <Box
                                    sx={{
                                        display: 'flex',
                                        alignItems: 'flex-start',
                                        justifyContent: 'space-between',
                                        gap: 2,
                                        flexWrap: 'wrap',
                                        mb: 3,
                                    }}
                                >
                                    <Box>
                                        <Typography variant="h3" sx={{ fontWeight: 800, mb: 1 }}>
                                            {project.name}
                                        </Typography>

                                        <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
                                            {project.type && (
                                                <Chip label={project.type} />
                                            )}

                                            {project.track && (
                                                <Chip label={project.track} variant="outlined" />
                                            )}

                                            {project.year && (
                                                <Chip label={`${project.year} год`} variant="outlined" />
                                            )}
                                        </Stack>
                                    </Box>

                                    {project.captainMine ? (
                                        <Chip label="Ваш проект" color="primary" />
                                    ) : project.hasCaptain ? (
                                        <Chip label="Занят" />
                                    ) : (
                                        <Chip label="Свободен" color="success" />
                                    )}
                                </Box>

                                {project.description && (
                                    <Typography
                                        color="text.secondary"
                                        sx={{
                                            whiteSpace: 'pre-line',
                                            lineHeight: 1.7,
                                            mb: 4,
                                        }}
                                    >
                                        {project.description}
                                    </Typography>
                                )}

                                <Stack spacing={1.5} sx={{ mb: 4 }}>
                                    <Typography>
                                        <Box component="span" sx={{ fontWeight: 700 }}>
                                            Технологии:
                                        </Box>{' '}
                                        {project.technologies || 'не указаны'}
                                    </Typography>

                                    <Typography>
                                        <Box component="span" sx={{ fontWeight: 700 }}>
                                            Количество участников:
                                        </Box>{' '}
                                        {project.quantityOfStudents ?? 'не указано'}
                                    </Typography>

                                    <Typography>
                                        <Box component="span" sx={{ fontWeight: 700 }}>
                                            Капитан:
                                        </Box>{' '}
                                        {project.captainUserFullName || 'не назначен'}
                                    </Typography>

                                    {project.captainLogin && (
                                        <Typography>
                                            <Box component="span" sx={{ fontWeight: 700 }}>
                                                GitHub:
                                            </Box>{' '}
                                            {project.captainLogin}
                                        </Typography>
                                    )}
                                </Stack>

                                {!project.hasCaptain && !hasMyProject && (
                                    <Button
                                        variant="contained"
                                        disabled={claiming}
                                        onClick={claimCaptain}
                                    >
                                        {claiming ? 'Сохраняем…' : 'Стать капитаном'}
                                    </Button>
                                )}

                                {project.captainMine && (
                                    <Alert severity="info">
                                        Вы капитан этого проекта. Ниже доступна форма сдачи материалов по этапам отчётности.
                                    </Alert>
                                )}

                                {!project.captainMine && hasMyProject && (
                                    <Alert severity="info">
                                        Вы уже привязаны к другому проекту как капитан.
                                    </Alert>
                                )}
                            </Box>
                        </Paper>

                        {project.captainMine && (
                            <ProjectSubmissionsForm projectId={project.id} />
                        )}
                    </>
                )}
            </Container>
        </Box>
    );
};

export default ProjectPage;