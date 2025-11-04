// ProjectEventManagement.js
import React, { useEffect, useState } from 'react';
import {
    Alert,
    Autocomplete,
    Box,
    Button,
    Checkbox,
    CircularProgress,
    Divider,
    FormControlLabel,
    Snackbar,
    Stack,
    TextField,
    Typography,
} from '@mui/material';
import { Title } from 'react-admin';

const ProjectEventManagement = () => {
    const [events, setEvents] = useState([]);
    const [projects, setProjects] = useState([]);

    const [selectedEvent, setSelectedEvent] = useState(null);
    const [selectedProjects, setSelectedProjects] = useState([]);
    const [linkAllProjects, setLinkAllProjects] = useState(false);

    const [loadingLists, setLoadingLists] = useState(false);
    const [saving, setSaving] = useState(false);

    const [snackbarOpen, setSnackbarOpen] = useState(false);
    const [snackbarMessage, setSnackbarMessage] = useState('');
    const [snackbarSeverity, setSnackbarSeverity] = useState('success');

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoadingLists(true);
                const [eventsRes, projectsRes] = await Promise.all([
                    fetch('/api/v1/admin/events?limit=100', { credentials: 'include' }),
                    fetch('/api/v1/admin/projects?limit=100', { credentials: 'include' }),
                ]);
                const [eventsData, projectsData] = await Promise.all([eventsRes.json(), projectsRes.json()]);
                setEvents(Array.isArray(eventsData.content) ? eventsData.content : []);
                setProjects(Array.isArray(projectsData.content) ? projectsData.content : []);
            } catch (e) {
                console.error(e);
            } finally {
                setLoadingLists(false);
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
                setLoadingLists(true);
                const res = await fetch(`/api/v1/admin/events/${selectedEvent}/projects?limit=100`, { credentials: 'include' });
                const data = await res.json();
                setSelectedProjects(Array.isArray(data.content) ? data.content : []);
            } catch (e) {
                console.error(e);
            } finally {
                setLoadingLists(false);
            }
        };
        fetchLinked();
    }, [selectedEvent, linkAllProjects]);

    const handleSave = async () => {
        if (!selectedEvent) return;
        setSaving(true);
        try {
            const payload = {
                eventId: selectedEvent,
                projectIds: linkAllProjects ? null : selectedProjects.map(p => p.id),
                linkAllProjects,
            };
            const res = await fetch(`/api/v1/admin/events/${selectedEvent}/projects?limit=100`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify(payload),
            });
            if (res.ok) {
                setSnackbarMessage('Успешно сохранено');
                setSnackbarSeverity('success');
            } else {
                const err = await res.json().catch(() => ({}));
                setSnackbarMessage(err.error || 'Во время сохранения произошла ошибка');
                setSnackbarSeverity('error');
            }
        } catch (e) {
            console.error(e);
            setSnackbarMessage('Во время сохранения произошла ошибка');
            setSnackbarSeverity('error');
        } finally {
            setSaving(false);
            setSnackbarOpen(true);
        }
    };

    const canSave = !!selectedEvent && !loadingLists;

    return (
        <>
            <Title title="Этапы отчётности" />
            <Box
                sx={{
                    maxWidth: 720,
                    width: '100%',
                    mx: 'auto',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: 2,
                }}
            >
                <Typography variant="h5" fontWeight={600}>
                    Привязка проектов к этапам отчётности
                </Typography>
                <Divider />

                <Stack spacing={3}>
                    <Autocomplete
                        options={events}
                        getOptionLabel={o => (o?.name ? `${o.name} ${o.year}-${parseInt(o.year, 10) + 1}` : '')}
                        onChange={(_, v) => {
                            setSelectedEvent(v?.id || null);
                            setLinkAllProjects(false);
                            setSelectedProjects([]);
                        }}
                        renderInput={params => <TextField {...params} label="Выберите этап отчётности" size="small" fullWidth />}
                        loading={loadingLists}
                        loadingText="Загрузка…"
                    />

                    {selectedEvent && (
                        <FormControlLabel
                            control={
                                <Checkbox
                                    checked={linkAllProjects}
                                    onChange={e => {
                                        setLinkAllProjects(e.target.checked);
                                        if (e.target.checked) setSelectedProjects([]);
                                    }}
                                />
                            }
                            label="Привязать все проекты за текущий год"
                        />
                    )}

                    {!linkAllProjects && selectedEvent && (
                        <Autocomplete
                            multiple
                            options={projects}
                            getOptionLabel={o => o?.name || ''}
                            isOptionEqualToValue={(o, v) => o.id === v.id}
                            value={selectedProjects}
                            onChange={(_, v) => setSelectedProjects(v)}
                            renderInput={params => (
                                <TextField {...params} label="Привязанные проекты" size="small" fullWidth />
                            )}
                            loading={loadingLists}
                            loadingText="Загрузка…"
                        />
                    )}

                    {selectedEvent && (
                        <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
                            <Button
                                variant="contained"
                                onClick={handleSave}
                                disabled={!canSave || saving}
                                startIcon={saving ? <CircularProgress size={18} /> : null}
                            >
                                {saving ? 'Сохранение…' : 'Сохранить'}
                            </Button>
                        </Box>
                    )}
                </Stack>
            </Box>

            <Snackbar
                open={snackbarOpen}
                autoHideDuration={3000}
                onClose={(_, r) => r !== 'clickaway' && setSnackbarOpen(false)}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
            >
                <Alert onClose={() => setSnackbarOpen(false)} severity={snackbarSeverity} sx={{ width: '100%' }}>
                    {snackbarMessage}
                </Alert>
            </Snackbar>
        </>
    );
};

export default ProjectEventManagement;
