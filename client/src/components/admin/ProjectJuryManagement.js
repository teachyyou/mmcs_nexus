import React, { useEffect, useState } from 'react';
import {
    Autocomplete,
    TextField,
    Button,
    Checkbox,
    FormControlLabel,
    Snackbar,
    Alert,
} from '@mui/material';

const ProjectJuryManagement = () => {
    const [projects, setProjects] = useState([]);
    const [selectedProject, setSelectedProject] = useState(null);
    const [events, setEvents] = useState([]);
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [willingJuries, setWillingJuries] = useState([]);
    const [obligedJuries, setObligedJuries] = useState([]);
    const [mentors, setMentors] = useState([]);
    const [juryOptions, setJuryOptions] = useState([]);
    const [applyToAllEvents, setApplyToAllEvents] = useState(false);
    const [openSnackbar, setOpenSnackbar] = useState(false);
    const [snackbarMessage, setSnackbarMessage] = useState('');
    const [snackbarSeverity, setSnackbarSeverity] = useState('success');
    const [loading, setLoading] = useState(false);

    // Получаем проекты и список жюри при монтировании
    useEffect(() => {
        const fetchInitialData = async () => {
            try {
                const projectsResponse = await fetch('http://localhost:8080/api/v1/admin/projects', {
                    credentials: 'include',
                });
                const projectsData = await projectsResponse.json();
                setProjects(Array.isArray(projectsData.content) ? projectsData.content : []);

                const juriesResponse = await fetch('http://localhost:8080/api/v1/admin/users', {
                    credentials: 'include',
                });
                const juriesData = await juriesResponse.json();
                setJuryOptions(Array.isArray(juriesData.content) ? juriesData.content : []);
            } catch (error) {
                console.error('Error fetching projects or juries:', error);
            }
        };

        fetchInitialData();
    }, []);

    // Получаем события для выбранного проекта, если не выбран режим "Apply to all events"
    useEffect(() => {
        const fetchEvents = async () => {
            if (selectedProject && !applyToAllEvents) {
                try {
                    const response = await fetch(`http://localhost:8080/api/v1/admin/projects/${selectedProject}/events`, {
                        credentials: 'include',
                    });
                    const data = await response.json();
                    const newEvents = Array.isArray(data.content) ? data.content : [];
                    setEvents(newEvents);

                    // Если текущее событие отсутствует в новом списке, сбрасываем его и очищаем выбранные жюри
                    if (!newEvents.some(event => event.id === selectedEvent)) {
                        setSelectedEvent(null);
                        setWillingJuries([]);
                        setObligedJuries([]);
                        setMentors([]);
                    }
                } catch (error) {
                    console.error('Error fetching events:', error);
                }
            } else {
                setEvents([]);
                setSelectedEvent(null);
                setWillingJuries([]);
                setObligedJuries([]);
                setMentors([]);
            }
        };

        fetchEvents();
    }, [selectedProject, applyToAllEvents, selectedEvent]);

    // Получаем назначенных жюри для выбранного проекта и события (или для всех событий)
    useEffect(() => {
        const fetchAssignedJuries = async () => {
            if (selectedProject && (selectedEvent || applyToAllEvents)) {
                try {
                    const url = applyToAllEvents
                        ? `http://localhost:8080/api/v1/admin/projects/${selectedProject}/juries`
                        : `http://localhost:8080/api/v1/admin/projects/${selectedProject}/juries/${selectedEvent}`;
                    const response = await fetch(url, { credentials: 'include' });
                    const data = await response.json();
                    setWillingJuries(data.willingJuries || []);
                    setObligedJuries(data.obligedJuries || []);
                    setMentors(data.mentors || []);
                } catch (error) {
                    console.error('Error fetching assigned juries:', error);
                }
            }
        };

        fetchAssignedJuries();
    }, [selectedProject, selectedEvent, applyToAllEvents]);

    // Обработчики изменения выбранных жюри
    const handleWillingJuriesChange = (event, newValue) => {
        setObligedJuries(prev => prev.filter(jury => !newValue.some(j => j.id === jury.id)));
        setMentors(prev => prev.filter(jury => !newValue.some(j => j.id === jury.id)));
        setWillingJuries(newValue);
    };

    const handleObligedJuriesChange = (event, newValue) => {
        setWillingJuries(prev => prev.filter(jury => !newValue.some(j => j.id === jury.id)));
        setMentors(prev => prev.filter(jury => !newValue.some(j => j.id === jury.id)));
        setObligedJuries(newValue);
    };

    const handleMentorsChange = (event, newValue) => {
        setWillingJuries(prev => prev.filter(jury => !newValue.some(j => j.id === jury.id)));
        setObligedJuries(prev => prev.filter(jury => !newValue.some(j => j.id === jury.id)));
        setMentors(newValue);
    };

    // Отправка изменений на сервер
    const handleSubmit = async () => {
        if (!selectedProject) return;
        setLoading(true);
        const payload = {
            projectId: selectedProject,
            eventId: applyToAllEvents ? null : selectedEvent,
            willingJuries: willingJuries.map(jury => jury.id),
            obligedJuries: obligedJuries.map(jury => jury.id),
            mentors: mentors.map(mentor => mentor.id),
            applyToAllEvents: applyToAllEvents,
        };

        try {
            const response = await fetch(`http://localhost:8080/api/v1/admin/projects/${selectedProject}/juries`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify(payload),
            });
            if (response.ok) {
                setSnackbarMessage('Saved successfully');
                setSnackbarSeverity('success');
            } else {
                setSnackbarMessage('Failed to save');
                setSnackbarSeverity('error');
            }
        } catch (error) {
            console.error('Error saving project juries:', error);
            setSnackbarMessage('Failed to save');
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

    // Фильтрация жюри, исключая уже выбранных в других ролях
    const getFilteredJuryOptions = (currentRole) => {
        const selectedIds = new Set([
            ...willingJuries.map(jury => jury.id),
            ...obligedJuries.map(jury => jury.id),
            ...mentors.map(jury => jury.id),
        ]);

        // Убираем ID уже выбранных в текущей роли
        if (currentRole === 'willing') {
            willingJuries.forEach(jury => selectedIds.delete(jury.id));
        } else if (currentRole === 'obliged') {
            obligedJuries.forEach(jury => selectedIds.delete(jury.id));
        } else if (currentRole === 'mentor') {
            mentors.forEach(jury => selectedIds.delete(jury.id));
        }

        return juryOptions.filter(jury => !selectedIds.has(jury.id));
    };

    return (
        <div>
            <h2>Manage Project Juries</h2>

            {/* Выбор проекта */}
            <Autocomplete
                options={projects}
                getOptionLabel={(option) => `${option.name} ${option.year}-${parseInt(option.year) + 1}`}
                onChange={(event, newValue) => {
                    setSelectedProject(newValue?.id || null);
                    setApplyToAllEvents(false);
                    setWillingJuries([]);
                    setObligedJuries([]);
                    setMentors([]);
                }}
                renderInput={(params) => <TextField {...params} label="Select Project" />}
                style={{ marginBottom: '16px' }}
            />

            {/* Галочка "Apply to all events" */}
            {selectedProject && (
                <FormControlLabel
                    control={
                        <Checkbox
                            checked={applyToAllEvents}
                            onChange={(e) => {
                                setApplyToAllEvents(e.target.checked);
                                if (e.target.checked) {
                                    setSelectedEvent(null);
                                }
                            }}
                        />
                    }
                    label="Apply to all events"
                />
            )}

            {/* Выбор события */}
            {!applyToAllEvents && selectedProject && (
                <Autocomplete
                    options={events}
                    value={events.find(event => event.id === selectedEvent) || null}
                    getOptionLabel={(option) => option.name}
                    onChange={(event, newValue) => {
                        setSelectedEvent(newValue?.id || null);
                        setWillingJuries([]);
                        setObligedJuries([]);
                        setMentors([]);
                    }}
                    renderInput={(params) => <TextField {...params} label="Select Event" />}
                    style={{ marginBottom: '16px' }}
                />
            )}

            {/* Выбор жюри */}
            {selectedProject && ((selectedEvent && !applyToAllEvents) || applyToAllEvents) && (
                <>
                    <Autocomplete
                        multiple
                        options={getFilteredJuryOptions('willing')}
                        getOptionLabel={(option) => `${option.firstName} ${option.lastName}`}
                        isOptionEqualToValue={(option, value) => option.id === value.id}
                        value={willingJuries}
                        onChange={handleWillingJuriesChange}
                        renderInput={(params) => <TextField {...params} label="Willing Juries" />}
                        style={{ marginBottom: '16px' }}
                    />

                    <Autocomplete
                        multiple
                        options={getFilteredJuryOptions('obliged')}
                        getOptionLabel={(option) => `${option.firstName} ${option.lastName}`}
                        isOptionEqualToValue={(option, value) => option.id === value.id}
                        value={obligedJuries}
                        onChange={handleObligedJuriesChange}
                        renderInput={(params) => <TextField {...params} label="Obliged Juries" />}
                        style={{ marginBottom: '16px' }}
                    />

                    <Autocomplete
                        multiple
                        options={getFilteredJuryOptions('mentor')}
                        getOptionLabel={(option) => `${option.firstName} ${option.lastName}`}
                        isOptionEqualToValue={(option, value) => option.id === value.id}
                        value={mentors}
                        onChange={handleMentorsChange}
                        renderInput={(params) => <TextField {...params} label="Mentors" />}
                        style={{ marginBottom: '16px' }}
                    />

                    <Button onClick={handleSubmit} variant="contained" color="primary" disabled={loading}>
                        {loading ? 'Saving...' : 'Save Changes'}
                    </Button>
                </>
            )}

            <Snackbar
                open={openSnackbar}
                autoHideDuration={3000}
                onClose={handleSnackbarClose}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
            >
                <Alert onClose={handleSnackbarClose} severity={snackbarSeverity} sx={{ width: '100%' }}>
                    {snackbarMessage}
                </Alert>
            </Snackbar>
        </div>
    );
};

export default ProjectJuryManagement;
