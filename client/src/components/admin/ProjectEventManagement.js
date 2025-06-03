import React, {useEffect, useState} from 'react';
import {Alert, Autocomplete, Button, Checkbox, FormControlLabel, Snackbar, TextField} from '@mui/material';
import {Title} from "react-admin";

const ProjectEventManagement = () => {
    const [events, setEvents] = useState([]);
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [projects, setProjects] = useState([]);
    const [selectedProjects, setSelectedProjects] = useState([]);
    const [linkAllProjects, setLinkAllProjects] = useState(false);
    const [openSnackbar, setOpenSnackbar] = useState(false);
    const [snackbarMessage, setSnackbarMessage] = useState('');
    const [snackbarSeverity, setSnackbarSeverity] = useState('success');
    const [loading, setLoading] = useState(false);

    // Загружаем список событий и проектов
    useEffect(() => {
        const fetchData = async () => {
            try {
                const eventsResponse = await fetch('http://localhost:8080/api/v1/admin/events', { credentials: 'include' });
                const eventsData = await eventsResponse.json();
                setEvents(Array.isArray(eventsData.content) ? eventsData.content : []);

                const projectsResponse = await fetch('http://localhost:8080/api/v1/admin/projects', { credentials: 'include' });
                const projectsData = await projectsResponse.json();
                setProjects(Array.isArray(projectsData.content) ? projectsData.content : []);
            } catch (error) {
                console.error('Error fetching events or projects:', error);
            }
        };

        fetchData();
    }, []);

    // Загружаем проекты, связанные с выбранным событием (если не выбрана опция "Link all projects")
    useEffect(() => {
        const fetchLinkedProjects = async () => {
            if (selectedEvent && !linkAllProjects) {
                try {
                    const response = await fetch(`http://localhost:8080/api/v1/admin/events/${selectedEvent}/projects`, { credentials: 'include' });
                    const data = await response.json();
                    setSelectedProjects(Array.isArray(data.content) ? data.content : []);
                } catch (error) {
                    console.error('Error fetching linked projects:', error);
                }
            } else {
                setSelectedProjects([]);
            }
        };

        fetchLinkedProjects();
    }, [selectedEvent, linkAllProjects]);

    // Отправка данных на сервер
    const handleSubmit = async () => {
        if (!selectedEvent) return;
        setLoading(true);

        const payload = {
            eventId: selectedEvent,
            projectIds: linkAllProjects ? null : selectedProjects.map(project => project.id),
            linkAllProjects: linkAllProjects
        };

        try {
            const response = await fetch(`http://localhost:8080/api/v1/admin/events/${selectedEvent}/projects`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
                body: JSON.stringify(payload),
            });
            if (!response.ok) {
                setSnackbarMessage('Во время сохранения произошла ошибка');
                setSnackbarSeverity('error');
            } else {
                setSnackbarMessage('Успешно сохранено');
                setSnackbarSeverity('success');
            }
        } catch (error) {
            console.error('Error saving changes:', error);
            setSnackbarMessage('Во время сохранения произошла ошибка');
            setSnackbarSeverity('error');
        } finally {
            setOpenSnackbar(true);
            setLoading(false);
        }
    };

    const handleSnackbarClose = (event, reason) => {
        if (reason === 'clickaway') return;
        setOpenSnackbar(false);
    };

    return (
        <>
            <Title title="Этапы отчётности" />
            <h2>Привязка проектов к этапам отчётности</h2>
            <div>
                {/* Выбор события */}
                <Autocomplete
                    options={events}
                    getOptionLabel={(option) => `${option.name} ${option.year}-${parseInt(option.year) + 1}`}
                    onChange={(event, newValue) => setSelectedEvent(newValue?.id || null)}
                    renderInput={(params) => <TextField {...params} label="Выберите этап отчётности"/>}
                    style={{marginBottom: '16px'}}
                />

                {/* Чекбокс "Link all projects for the same year" */}
                {selectedEvent && (
                    <FormControlLabel
                        control={
                            <Checkbox
                                checked={linkAllProjects}
                                onChange={(e) => setLinkAllProjects(e.target.checked)}
                            />
                        }
                        label="Привязать всё проекты за текущий год"
                    />
                )}

                {/* Редактирование связанных проектов (если не выбрана опция "Link all" и событие выбрано) */}
                {!linkAllProjects && selectedEvent && (
                    <Autocomplete
                        multiple
                        options={projects}
                        getOptionLabel={(option) => option.name}
                        isOptionEqualToValue={(option, value) => option.id === value.id}
                        value={selectedProjects}
                        onChange={(event, newValue) => setSelectedProjects(newValue)}
                        renderInput={(params) => <TextField {...params} label="Привязанные проекты"/>}
                        style={{marginTop: '16px', marginBottom: '16px'}}
                    />
                )}

                {/* Кнопка сохранения изменений */}
                {selectedEvent && (
                    <Button onClick={handleSubmit} variant="contained" color="primary" disabled={loading}>
                        {loading ? 'Отправка' : 'Сохранить'}
                    </Button>
                )}

                {/* Snackbar для вывода сообщений */}
                <Snackbar
                    open={openSnackbar}
                    autoHideDuration={3000}
                    onClose={handleSnackbarClose}
                    anchorOrigin={{vertical: 'bottom', horizontal: 'center'}}
                >
                    <Alert onClose={handleSnackbarClose} severity={snackbarSeverity} sx={{width: '100%'}}>
                        {snackbarMessage}
                    </Alert>
                </Snackbar>
            </div>
        </>
    );
};

export default ProjectEventManagement;
