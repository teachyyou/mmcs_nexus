import React from 'react';
import { Modal, Box, Typography, TextField, Button } from '@mui/material';

const GradeEditorModal = ({ open, onClose, grade, projectId, juryId, grades }) => {
    const project = grades.projects.find((proj) => proj.id === projectId);
    const jury = grades.juries.find((jur) => jur.id === juryId);
    const event = grades.event;

    const [comment, setComment] = React.useState(grade ? grade.comment : '');
    const [presPoints, setPresPoints] = React.useState(grade ? grade.presPoints : '');
    const [buildPoints, setBuildPoints] = React.useState(grade ? grade.buildPoints : '');

    React.useEffect(() => {
        if (grade) {
            setComment(grade.comment);
            setPresPoints(grade.presPoints);
            setBuildPoints(grade.buildPoints);
        } else {
            setComment('');
            setPresPoints('');
            setBuildPoints('');
        }
    }, [grade]);

    const handleSave = async () => {
        const gradeData = {
            projectId: projectId,
            eventId: event.id,
            comment,
            presPoints: parseInt(presPoints, 10),
            buildPoints: parseInt(buildPoints, 10)
        };

        try {
            const response = await fetch('http://localhost:8080/api/v1/jury/grades', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
                body: JSON.stringify(gradeData)
            });

            if (response.ok) {
                console.log("Оценка успешно сохранена:", gradeData);
                onClose();
            } else {
                const errorData = await response.json();
                console.error("Ошибка при сохранении оценки:", errorData);
            }
        } catch (error) {
            console.error("Ошибка при отправке запроса:", error);
        }
    };

    return (
        <Modal open={open} onClose={onClose}>
            <Box sx={{ p: 3, bgcolor: 'background.paper', borderRadius: 2, width: 400, mx: 'auto', my: '15%' }}>
                <Typography variant="h6" gutterBottom>Редактирование оценки</Typography>

                <Typography variant="body1" gutterBottom>
                    <strong>Название проекта:</strong> {project?.name || "Проект неизвестен"}
                </Typography>

                <Typography variant="body1" gutterBottom>
                    <strong>Проверяющий:</strong> {jury?.firstName} {jury?.lastName || "Жюри неизвестно"}
                </Typography>

                <Typography variant="body1" gutterBottom>
                    <strong>Событие:</strong> {event?.name || "Событие неизвестно"}
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
                />
                <TextField
                    label="Очки за сборку"
                    type="number"
                    fullWidth
                    value={buildPoints}
                    onChange={(e) => setBuildPoints(e.target.value)}
                    margin="normal"
                />
                <Button variant="contained" color="primary" onClick={handleSave} sx={{ mt: 2 }}>
                    Сохранить
                </Button>
            </Box>
        </Modal>
    );
};

export default GradeEditorModal;
