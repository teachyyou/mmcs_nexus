import React, { useState, useEffect } from 'react';
import {
    Modal,
    Box,
    Typography,
    TextField,
    Button,
    InputAdornment
} from '@mui/material';

const GradeEditorModal = ({
                              open,
                              onClose,
                              grade,
                              projectId,
                              juryId,
                              grades,
                              onGradeUpdated
                          }) => {
    const project = grades.projects.find(proj => proj.id === projectId);
    const jury = grades.juries.find(jur => jur.id === juryId);
    const event = grades.event;

    const [comment, setComment] = useState('');
    const [presPoints, setPresPoints] = useState('');
    const [buildPoints, setBuildPoints] = useState('');

    // Вычисляем, есть ли ошибка для поля презентации
    const presError =
        presPoints !== '' &&
        (parseInt(presPoints, 10) < 0 || parseInt(presPoints, 10) > event.maxPresPoints);

    // Вычисляем, есть ли ошибка для поля сборки
    const buildError =
        buildPoints !== '' &&
        (parseInt(buildPoints, 10) < 0 || parseInt(buildPoints, 10) > event.maxBuildPoints);

    useEffect(() => {
        if (!open) return;
        if (grade) {
            setComment(grade.comment || '');
            setPresPoints(
                grade.presPoints !== undefined ? grade.presPoints : ''
            );
            setBuildPoints(
                grade.buildPoints !== undefined ? grade.buildPoints : ''
            );
        } else {
            setComment('');
            setPresPoints('');
            setBuildPoints('');
        }
    }, [grade, open]);

    const handleSave = async () => {
        const gradeData = {
            projectId: projectId,
            eventId: event.id,
            comment,
            presPoints: presPoints === '' ? '' : parseInt(presPoints, 10),
            buildPoints: buildPoints === '' ? '' : parseInt(buildPoints, 10)
        };

        try {
            const method = grade ? 'PUT' : 'POST';
            const response = await fetch(
                'http://localhost:8080/api/v1/jury/grades',
                {
                    method,
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body: JSON.stringify(gradeData)
                }
            );
            if (response.ok) {
                const updatedGrade = await response.json();
                if (onGradeUpdated) {
                    onGradeUpdated(updatedGrade);
                }
                onClose();
            } else {
                const errorData = await response.json();
                console.error('Ошибка при сохранении оценки:', errorData);
            }
        } catch (error) {
            console.error('Ошибка при отправке запроса:', error);
        }
    };

    return (
        <Modal open={open} onClose={onClose}>
            <Box
                sx={{
                    p: 3,
                    bgcolor: 'background.paper',
                    borderRadius: 2,
                    width: 400,
                    mx: 'auto',
                    my: '15%'
                }}
            >
                <Typography variant="h6" gutterBottom>
                    {grade ? 'Редактирование оценки' : 'Создание оценки'}
                </Typography>
                <Typography variant="body1" gutterBottom>
                    <strong>Название проекта:</strong>{' '}
                    {project?.name || 'Проект неизвестен'}
                </Typography>
                <Typography variant="body1" gutterBottom>
                    <strong>Проверяющий:</strong> {jury?.firstName}{' '}
                    {jury?.lastName || 'Жюри неизвестно'}
                </Typography>
                <Typography variant="body1" gutterBottom>
                    <strong>Событие:</strong> {event?.name || 'Событие неизвестно'}
                </Typography>
                <TextField
                    label="Комментарий"
                    fullWidth
                    value={comment}
                    onChange={(e) => setComment(e.target.value)}
                    margin="normal"
                />
                <TextField
                    label="Очки за презентацию"
                    type="number"
                    fullWidth
                    value={presPoints}
                    onChange={(e) => setPresPoints(e.target.value)}
                    margin="normal"
                    variant="outlined"
                    error={presError}
                    helperText={
                        presError
                            ? `Значение должно быть от 0 до ${event.maxPresPoints}`
                            : ''
                    }
                    InputProps={{
                        inputProps: { min: 0, max: event.maxPresPoints },
                        endAdornment: (
                            <InputAdornment position="end">
                                <Typography
                                    variant="subtitle1"
                                    sx={{ fontWeight: 'bold', color: 'text.secondary' }}
                                    component="span"
                                >
                                    /{event.maxPresPoints}
                                </Typography>
                            </InputAdornment>
                        )
                    }}
                />
                <TextField
                    label="Очки за сборку"
                    type="number"
                    fullWidth
                    value={buildPoints}
                    onChange={(e) => setBuildPoints(e.target.value)}
                    margin="normal"
                    variant="outlined"
                    error={buildError}
                    helperText={
                        buildError
                            ? `Значение должно быть от 0 до ${event.maxBuildPoints}`
                            : ''
                    }
                    InputProps={{
                        inputProps: { min: 0, max: event.maxBuildPoints },
                        endAdornment: (
                            <InputAdornment position="end">
                                <Typography
                                    variant="subtitle1"
                                    sx={{ fontWeight: 'bold', color: 'text.secondary' }}
                                    component="span"
                                >
                                    /{event.maxBuildPoints}
                                </Typography>
                            </InputAdornment>
                        )
                    }}
                />
                <Button
                    variant="contained"
                    color="primary"
                    onClick={handleSave}
                    sx={{ mt: 2 }}
                    disabled={presError || buildError}
                >
                    Сохранить
                </Button>
            </Box>
        </Modal>
    );
};

export default GradeEditorModal;
