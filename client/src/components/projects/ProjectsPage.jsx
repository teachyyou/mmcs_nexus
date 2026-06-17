import React, { useEffect, useState } from 'react';
import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    Chip,
    CircularProgress,
    Container,
    Divider,
    Stack,
    Typography,
} from '@mui/material';
import { Link } from 'react-router-dom';
import { useAuth } from '../../AuthContext';

const ProjectsPage = () => {
    const { captainProject, setCaptainProject } = useAuth();

    const [projects, setProjects] = useState([]);
    const [total, setTotal] = useState(0);
    const [offset, setOffset] = useState(0);
    const [loading, setLoading] = useState(true);
    const [loadingMore, setLoadingMore] = useState(false);
    const [claimingProjectId, setClaimingProjectId] = useState(null);
    const [error, setError] = useState(null);
    const [successMessage, setSuccessMessage] = useState(null);

    const limit = 10;

    const loadProjects = async (nextOffset = 0, append = false) => {
        append ? setLoadingMore(true) : setLoading(true);
        setError(null);

        try {
            const response = await fetch(
                `/api/v1/user/projects?limit=${limit}&offset=${nextOffset}&sort=name&order=asc`,
                { credentials: 'include' }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || 'Не удалось загрузить проекты');
            }

            const items = data.data || data.content || [];

            setProjects((current) => append ? [...current, ...items] : items);
            setTotal(data.total ?? data.totalElements ?? items.length);
            setOffset(nextOffset);
        } catch (requestError) {
            setError(requestError.message || 'Ошибка загрузки проектов');
        } finally {
            append ? setLoadingMore(false) : setLoading(false);
        }
    };

    useEffect(() => {
        loadProjects();
    }, []);

    const claimCaptain = async (projectId) => {
        setClaimingProjectId(projectId);
        setError(null);
        setSuccessMessage(null);

        try {
            const response = await fetch(`/api/v1/user/projects/${projectId}/claim-captain`, {
                method: 'POST',
                credentials: 'include',
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || 'Не удалось стать капитаном проекта');
            }

            const project = data.project;

            setProjects((current) => current.map((item) => (
                item.id === project.id ? project : item
            )));

            setCaptainProject({
                id: project.id,
                name: project.name,
            });

            setSuccessMessage(`Вы стали капитаном проекта «${project.name}»`);
        } catch (requestError) {
            setError(requestError.message || 'Ошибка привязки проекта');
        } finally {
            setClaimingProjectId(null);
        }
    };

    const hasMore = projects.length < total;
    const hasMyProject = Boolean(captainProject);

    return (
        <Box sx={{ pt: 10, pb: 6, minHeight: '100vh' }}>
            <Container maxWidth="md">
                <Box sx={{ mb: 4 }}>
                    <Typography variant="h3" sx={{ fontWeight: 800, mb: 1 }}>
                        Проекты
                    </Typography>
                    <Typography color="text.secondary">
                        Найдите свой проект и привяжитесь к нему как капитан команды.
                    </Typography>
                </Box>

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

                {!loading && !error && projects.length === 0 && (
                    <Alert severity="info">
                        Проекты за текущий год пока не найдены
                    </Alert>
                )}

                <Stack spacing={0}>
                    {projects.map((project, index) => (
                        <React.Fragment key={project.id}>
                            {index > 0 && <Divider sx={{ my: 3 }} />}

                            <Card sx={{ borderRadius: 3 }}>
                                <CardContent sx={{ p: 3 }}>
                                    <Box
                                        sx={{
                                            display: 'flex',
                                            alignItems: 'flex-start',
                                            justifyContent: 'space-between',
                                            gap: 2,
                                            flexWrap: 'wrap',
                                            mb: 2,
                                        }}
                                    >
                                        <Box>
                                            <Typography variant="h5" sx={{ fontWeight: 750, mb: 1 }}>
                                                {project.name}
                                            </Typography>

                                            <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
                                                {project.type && (
                                                    <Chip size="small" label={project.type} />
                                                )}

                                                {project.track && (
                                                    <Chip size="small" label={project.track} variant="outlined" />
                                                )}

                                                {project.full && (
                                                    <Chip size="small" label="Команда набрана" color="success" variant="outlined" />
                                                )}
                                            </Stack>
                                        </Box>

                                        {project.captainMine ? (
                                            <Chip label="Ваш проект" color="primary" />
                                        ) : project.hasCaptain ? (
                                            <Chip label="Занят" color="default" />
                                        ) : (
                                            <Chip label="Свободен" color="success" />
                                        )}
                                    </Box>

                                    {project.description && (
                                        <Typography
                                            color="text.secondary"
                                            sx={{
                                                whiteSpace: 'pre-line',
                                                mb: 2,
                                            }}
                                        >
                                            {project.description}
                                        </Typography>
                                    )}

                                    <Stack spacing={0.75} sx={{ mb: 2 }}>
                                        {project.technologies && (
                                            <Typography variant="body2">
                                                <Box component="span" sx={{ fontWeight: 700 }}>
                                                    Технологии:
                                                </Box>{' '}
                                                {project.technologies}
                                            </Typography>
                                        )}

                                        <Typography variant="body2">
                                            <Box component="span" sx={{ fontWeight: 700 }}>
                                                Количество участников:
                                            </Box>{' '}
                                            {project.quantityOfStudents ?? 'не указано'}
                                        </Typography>

                                        <Typography variant="body2">
                                            <Box component="span" sx={{ fontWeight: 700 }}>
                                                Капитан:
                                            </Box>{' '}
                                            {project.captainUserFullName || 'не назначен'}
                                        </Typography>
                                    </Stack>

                                    <Box
                                        sx={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            justifyContent: 'space-between',
                                            gap: 2,
                                            flexWrap: 'wrap',
                                            mt: 3,
                                        }}
                                    >
                                        <Button
                                            component={Link}
                                            to={`/projects/${project.id}`}
                                            variant="text"
                                        >
                                            Подробнее
                                        </Button>

                                        {!project.hasCaptain && !hasMyProject && (
                                            <Button
                                                variant="contained"
                                                disabled={claimingProjectId === project.id}
                                                onClick={() => claimCaptain(project.id)}
                                            >
                                                {claimingProjectId === project.id ? 'Сохраняем…' : 'Стать капитаном'}
                                            </Button>
                                        )}
                                    </Box>
                                </CardContent>
                            </Card>
                        </React.Fragment>
                    ))}
                </Stack>

                {hasMore && (
                    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                        <Button
                            variant="contained"
                            disabled={loadingMore}
                            onClick={() => loadProjects(offset + limit, true)}
                        >
                            {loadingMore ? 'Загрузка…' : 'Показать ещё'}
                        </Button>
                    </Box>
                )}
            </Container>
        </Box>
    );
};

export default ProjectsPage;