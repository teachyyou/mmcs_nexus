import React, { useState, useEffect } from 'react';
import { Modal, Box, Typography, TextField, Button, InputAdornment } from '@mui/material';
import { useAuth } from '../../AuthContext';

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

    const { userId } = useAuth();
    const isOwner = userId === juryId;

    const [comment, setComment] = useState('');
    const [presPoints, setPresPoints] = useState('');
    const [buildPoints, setBuildPoints] = useState('');

    const presError =
        presPoints !== '' &&
        (parseInt(presPoints, 10) < 0 || parseInt(presPoints, 10) > event.maxPresPoints);
    const buildError =
        buildPoints !== '' &&
        (parseInt(buildPoints, 10) < 0 || parseInt(buildPoints, 10) > event.maxBuildPoints);

    useEffect(() => {
        if (!open) return;
        if (grade) {
            setComment(grade.comment || '');
            setPresPoints(grade.presPoints !== null ? grade.presPoints : '');
            setBuildPoints(grade.buildPoints !== null ? grade.buildPoints : '');

        } else {
            setComment('');
            setPresPoints('');
            setBuildPoints('');
        }
    }, [grade, open]);

    const handleSave = async () => {
        const gradeData = {
            projectId,
            eventId: event.id,
            comment,
            presPoints: presPoints === '' ? '' : parseInt(presPoints, 10),
            buildPoints: buildPoints === '' ? '' : parseInt(buildPoints, 10)
        };
        try {
            const method = grade ? 'PUT' : 'POST';
            const response = await fetch(
                '/api/v1/jury/grades',
                {
                    method,
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body: JSON.stringify(gradeData)
                }
            );
            if (response.ok) {
                const updatedGrade = await response.json();
                onGradeUpdated && onGradeUpdated(updatedGrade);
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
                    {isOwner ? (grade ? 'Редактирование оценки' : 'Создание оценки') : 'Просмотр оценки'}
                </Typography>
                <Typography variant="body1" gutterBottom>
                    <strong>Название проекта:</strong> {project?.name || 'Проект неизвестен'}
                </Typography>
                <Typography variant="body1" gutterBottom>
                    <strong>Проверяющий:</strong> {jury?.firstName} {jury?.lastName || 'Жюри неизвестно'}
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
                    InputProps={{ readOnly: !isOwner }}
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
                    helperText={presError ? `Значение должно быть от 0 до ${event.maxPresPoints}` : ''}
                    InputProps={{
                        readOnly: !isOwner,
                        inputProps: { min: 0, max: event.maxPresPoints },
                        endAdornment: <InputAdornment position="end">/{event.maxPresPoints}</InputAdornment>
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
                    helperText={buildError ? `Значение должно быть от 0 до ${event.maxBuildPoints}` : ''}
                    InputProps={{
                        readOnly: !isOwner,
                        inputProps: { min: 0, max: event.maxBuildPoints },
                        endAdornment: <InputAdornment position="end">/{event.maxBuildPoints}</InputAdornment>
                    }}
                />
                <Button
                    variant="contained"
                    color="primary"
                    onClick={isOwner ? handleSave : onClose}
                    sx={{ mt: 2 }}
                >
                    {isOwner ? 'Сохранить' : 'Закрыть'}
                </Button>
            </Box>
        </Modal>
    );
};

export default GradeEditorModal;
