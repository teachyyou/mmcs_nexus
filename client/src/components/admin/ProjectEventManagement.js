import React, { useEffect, useState } from 'react';
import { Autocomplete, TextField, Button, Checkbox, FormControlLabel, Snackbar, Alert } from '@mui/material';

const ProjectEventManagement = () => {
    const [events, setEvents] = useState([]); // Список событий
    const [selectedEvent, setSelectedEvent] = useState(null); // Выбранное событие
    const [projects, setProjects] = useState([]); // Список всех проектов
    const [selectedProjects, setSelectedProjects] = useState([]); // Связанные с событием проекты
    const [linkAllProjects, setLinkAllProjects] = useState(false); // Галочка "Link all projects for the same year"
    const [openSnackbar, setOpenSnackbar] = useState(false); // Состояние для Snackbar
    const [snackbarMessage, setSnackbarMessage] = useState(''); // Сообщение для Snackbar
    const [snackbarSeverity, setSnackbarSeverity] = useState('success'); // Тип сообщения (success или error)

    // Загрузка данных с бэка (список событий и всех проектов)
    useEffect(() => {
        // Fetch all available events
        fetch('http://localhost:8080/api/v1/admin/events')
            .then((response) => response.json())
            .then((data) => {
                setEvents(Array.isArray(data.content) ? data.content : []);
            })
            .catch((error) => console.error('Error fetching events:', error));

        // Fetch all available projects
        fetch('http://localhost:8080/api/v1/admin/projects')
            .then((response) => response.json())
            .then((data) => {
                setProjects(Array.isArray(data.content) ? data.content : []);
            })
            .catch((error) => console.error('Error fetching projects:', error));
    }, []);

    // Загрузка проектов, связанных с выбранным событием
    useEffect(() => {
        if (selectedEvent && !linkAllProjects) {
            // Fetch projects linked to the selected event
            fetch(`http://localhost:8080/api/v1/admin/events/${selectedEvent}/projects`)
                .then((response) => response.json())
                .then((data) => {
                    setSelectedProjects(Array.isArray(data.content) ? data.content : []);
                })
                .catch((error) => console.error('Error fetching linked projects:', error));
        } else {
            setSelectedProjects([]); // Очистить список, если не выбрано событие или выбрана опция "Link all projects for the same year"
        }
    }, [selectedEvent, linkAllProjects]);

    // Отправка изменений на бэк
    const handleSubmit = () => {
        const data = {
            eventId: selectedEvent,
            projectIds: linkAllProjects ? null : selectedProjects.map(project => project.id), // Если "Link all projects", projectIds = null
            linkAllProjects: linkAllProjects // Признак применения ко всем проектам за тот же год
        };

        fetch(`http://localhost:8080/api/v1/admin/events/${selectedEvent}/projects`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        })
            .then((response) => {
                if (!response.ok) {
                    setSnackbarMessage('Failed to save');
                    setSnackbarSeverity('error');
                    setOpenSnackbar(true);
                } else {
                    setSnackbarMessage('Saved successfully');
                    setSnackbarSeverity('success');
                    setOpenSnackbar(true);
                }
            })
            .catch((error) => {
                setSnackbarMessage('Failed to save');
                setSnackbarSeverity('error');
                setOpenSnackbar(true);
            });
    };

    // Обработчик закрытия Snackbar
    const handleSnackbarClose = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenSnackbar(false);
    };

    return (
        <div>
            <h2>Manage Projects for Event</h2>

            {/* Select Event */}
            <Autocomplete
                options={events}
                getOptionLabel={(option) => `${option.name} ${option.year}-${parseInt(option.year) + 1}`}
                onChange={(event, newValue) => setSelectedEvent(newValue?.id || null)}
                renderInput={(params) => <TextField {...params} label="Select Event" />}
            />

            {/* Checkbox for "Link all projects for the same year" */}
            {selectedEvent && (
                <FormControlLabel
                    control={
                        <Checkbox
                            checked={linkAllProjects}
                            onChange={(e) => setLinkAllProjects(e.target.checked)}
                        />
                    }
                    label="Link all projects for the same year"
                />
            )}

            {/* Edit linked projects (if not "Link all" and an event is selected) */}
            {!linkAllProjects && selectedEvent && (
                <Autocomplete
                    multiple
                    options={projects}
                    getOptionLabel={(option) => `${option.name}`}
                    isOptionEqualToValue={(option, value) => option.id === value.id}
                    value={selectedProjects} // Set selected projects
                    onChange={(event, newValue) => setSelectedProjects(newValue)}
                    renderInput={(params) => <TextField {...params} label="Linked Projects" />}
                />
            )}

            {/* Save Changes Button */}
            {selectedEvent && (
                <Button onClick={handleSubmit} variant="contained" color="primary">
                    Save Changes
                </Button>
            )}

            {/* Snackbar for displaying messages */}
            <Snackbar
                open={openSnackbar}
                autoHideDuration={3000} // Message will disappear after 3 seconds
                onClose={handleSnackbarClose}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }} // Position of the message
            >
                <Alert onClose={handleSnackbarClose} severity={snackbarSeverity} sx={{ width: '100%' }}>
                    {snackbarMessage}
                </Alert>
            </Snackbar>
        </div>
    );
};

export default ProjectEventManagement;
