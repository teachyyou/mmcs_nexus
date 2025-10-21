import React, { useEffect, useState } from 'react';
import {
    Alert,
    Autocomplete,
    Button,
    Checkbox,
    FormControlLabel,
    Snackbar,
    TextField,
    Stack,
    CircularProgress,
    Typography,
} from '@mui/material';
import { Title } from 'react-admin';

const ProjectEventManagement = () => {
    const [events, setEvents] = useState([]);
    const [projects, setProjects] = useState([]);
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [selectedProjects, setSelectedProjects] = useState([]);
    const [linkAllProjects, setLinkAllProjects] = useState(false);
    const [loadingFetch, setLoadingFetch] = useState(false);
    const [loadingSave, setLoadingSave] = useState(false);
    const [openSnackbar, setOpenSnackbar] = useState(false);
    const [snackbarMessage, setSnackbarMessage] = useState('');
    const [snackbarSeverity, setSnackbarSeverity] = useState('success');

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoadingFetch(true);
                const [eventsRes, projectsRes] = await Promise.all([
                    fetch('/api/v1/admin/events', { credentials: 'include' }),
                    fetch('/api/v1/admin/projects', { credentials: 'include' }),
                ]);
                const [eventsData, projectsData] = await Promise.all([
                    eventsRes.json(),
                    projectsRes.json(),
                ]);
                setEvents(Array.isArray(eventsData.content) ? eventsData.content : []);
                setProjects(Array.isArray(projectsData.content) ? projectsData.content : []);
            } catch (error) {
                console.error('Error fetching events or projects:', error);
            } finally {
                setLoadingFetch(false);
            }
        };
        fetchData();
    }, []);

    useEffect(() => {
        const fetchLinked = async () => {
            if (!selectedEvent || linkAllProjects) {
                setSelectedProjects([]);
                return;
            }
            try {
                setLoadingFetch(true);
                const response = await fetch(`/api/v1/admin/events/${selectedEvent}/projects`, {
                    credentials: 'include',
                });
                const data = await response.json();
                setSelectedProjects(Array.isArray(data.content) ? data.content : []);
            } catch (error) {
                console.error('Error fetching linked projects:', error);
            } finally {
                setLoadingFetch(false);
            }
        };
        fetchLinked();
    }, [selectedEvent, linkAllProjects]);

    const handleSubmit = async () => {
        if (!selectedEvent) return;
        setLoadingSave(true);
        const payload = {
            eventId: selectedEvent,
            projectIds: linkAllProjects ? null : selectedProjects.map(p => p.id),
            linkAllProjects,
        };
        try {
            const response = await fetch(`/api/v1/admin/events/${selectedEvent}/projects`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify(payload),
            });
            if (response.ok) {
                setSnackbarMessage('Изменения сохранены');
                setSnackbarSeverity('success');
            } else {
                const err = await response.json().catch(() => ({}));
                setSnackbarMessage(err.error || 'Во время сохранения произошла ошибка');
                setSnackbarSeverity('error');
            }
        } catch (error) {
            console.error('Error saving changes:', error);
            setSnackbarMessage('Сетевая ошибка: не удалось сохранить');
            setSnackbarSeverity('error');
        } finally {
            setLoadingSave(false);
            setOpenSnackbar(true);
        }
    };

    const handleSnackbarClose = (_, reason) => {
        if (reason === 'clickaway') return;
        setOpenSnackbar(false);
    };

    return (
        <>
            <Title title="Этапы отчётности" />
            <Typography variant="h5" sx={{ mb: 2 }}>Привязка проектов к этапам отчётности</Typography>

            <Stack spacing={3} sx={{ maxWidth: 720, width: '100%' }}>
                <Autocomplete
                    options={events}
                    fullWidth
                    getOptionLabel={o => `${o.name} ${o.year}-${parseInt(o.year) + 1}`}
                    onChange={(_, val) => setSelectedEvent(val?.id || null)}
                    renderInput={params => <TextField {...params} label="Выберите этап отчётности" />}
                />

                {selectedEvent && (
                    <FormControlLabel
                        control={
                            <Checkbox
                                checked={linkAllProjects}
                                onChange={e => setLinkAllProjects(e.target.checked)}
                            />
                        }
                        label="Привязать все проекты за текущий год"
                    />
                )}

                {loadingFetch && <CircularProgress />}

                {!linkAllProjects && selectedEvent && !loadingFetch && (
                    <Autocomplete
                        multiple
                        fullWidth
                        options={projects}
                        getOptionLabel={o => o.name}
                        isOptionEqualToValue={(o, v) => o.id === v.id}
                        value={selectedProjects}
                        onChange={(_, val) => setSelectedProjects(val)}
                        renderInput={params => <TextField {...params} label="Привязанные проекты" />}
                    />
                )}

                {selectedEvent && (
                    <Stack direction="row" spacing={2} justifyContent="flex-end">
                        <Button
                            variant="contained"
                            onClick={handleSubmit}
                            disabled={loadingSave}
                        >
                            {loadingSave ? 'Отправка…' : 'Сохранить'}
                        </Button>
                    </Stack>
                )}
            </Stack>

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
        </>
    );
};

export default ProjectEventManagement;
