// EventProjectDayAssignment.js
import React, { useEffect, useState } from 'react';
import {
    Alert,
    Autocomplete,
    Box,
    Button,
    CircularProgress,
    Divider,
    Snackbar,
    Stack,
    TextField,
    Typography,
} from '@mui/material';
import { Title } from 'react-admin';

const EventProjectDayAssignment = () => {
    const [events, setEvents] = useState([]);
    const [selectedEvent, setSelectedEvent] = useState(null);

    const [allProjects, setAllProjects] = useState([]);
    const [day1Projects, setDay1Projects] = useState([]);
    const [day2Projects, setDay2Projects] = useState([]);

    const [loadingLists, setLoadingLists] = useState(false);
    const [saving, setSaving] = useState(false);

    const [snackbarOpen, setSnackbarOpen] = useState(false);
    const [snackbarMessage, setSnackbarMessage] = useState('');
    const [snackbarSeverity, setSnackbarSeverity] = useState('success');

    useEffect(() => {
        const fetchEvents = async () => {
            try {
                setLoadingLists(true);
                const res = await fetch('/api/v1/admin/events?limit=100', { credentials: 'include' });
                const data = await res.json();
                setEvents(Array.isArray(data.content) ? data.content : []);
            } catch (e) {
                console.error(e);
            } finally {
                setLoadingLists(false);
            }
        };
        fetchEvents();
    }, []);

    useEffect(() => {
        if (!selectedEvent) {
            setAllProjects([]);
            setDay1Projects([]);
            setDay2Projects([]);
            return;
        }
        const fetchAll = async () => {
            try {
                setLoadingLists(true);
                const [resAll, resDays] = await Promise.all([
                    fetch(`/api/v1/admin/events/${selectedEvent}/projects?limit=100`, { credentials: 'include' }),
                    fetch(`/api/v1/admin/events/${selectedEvent}/projects/days?limit=100`, { credentials: 'include' }),
                ]);
                const dataAll = await resAll.json();
                const dataDays = await resDays.json();
                setAllProjects(Array.isArray(dataAll.content) ? dataAll.content : []);
                const content = dataDays.content || {};
                setDay1Projects(Array.isArray(content.firstDayProjects) ? content.firstDayProjects : []);
                setDay2Projects(Array.isArray(content.secondDayProjects) ? content.secondDayProjects : []);
            } catch (e) {
                console.error(e);
            } finally {
                setLoadingLists(false);
            }
        };
        fetchAll();
    }, [selectedEvent]);

    const handleSave = async () => {
        if (!selectedEvent) return;
        setSaving(true);
        try {
            const payload = {
                firstDayProjects: day1Projects.map(p => p.id),
                secondDayProjects: day2Projects.map(p => p.id),
            };
            const res = await fetch(`/api/v1/admin/events/${selectedEvent}/days`, {
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

    return (
        <>
            <Title title="Дни защиты" />
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
                    Распределение проектов по дням защиты
                </Typography>
                <Divider />

                <Stack spacing={3}>
                    <Autocomplete
                        options={events}
                        getOptionLabel={o => (o?.name ? `${o.name} ${o.year}-${parseInt(o.year, 10) + 1}` : '')}
                        onChange={(_, v) => setSelectedEvent(v?.id || null)}
                        renderInput={params => <TextField {...params} label="Выберите этап отчётности" size="small" fullWidth />}
                        loading={loadingLists}
                        loadingText="Загрузка…"
                    />

                    {selectedEvent && (
                        <Stack spacing={3}>
                            <Stack spacing={1.5}>
                                <Typography variant="subtitle1" fontWeight={600}>
                                    1-й день защиты
                                </Typography>
                                <Autocomplete
                                    multiple
                                    options={allProjects}
                                    getOptionLabel={o => o?.name || ''}
                                    isOptionEqualToValue={(o, v) => o.id === v.id}
                                    value={day1Projects}
                                    onChange={(_, v) => setDay1Projects(v)}
                                    renderInput={params => (
                                        <TextField {...params} label="Проекты первого дня" size="small" fullWidth />
                                    )}
                                    loading={loadingLists}
                                    loadingText="Загрузка…"
                                    getOptionDisabled={o => day2Projects.some(p => p.id === o.id)}
                                />
                            </Stack>

                            <Stack spacing={1.5}>
                                <Typography variant="subtitle1" fontWeight={600}>
                                    2-й день защиты
                                </Typography>
                                <Autocomplete
                                    multiple
                                    options={allProjects}
                                    getOptionLabel={o => o?.name || ''}
                                    isOptionEqualToValue={(o, v) => o.id === v.id}
                                    value={day2Projects}
                                    onChange={(_, v) => setDay2Projects(v)}
                                    renderInput={params => (
                                        <TextField {...params} label="Проекты второго дня" size="small" fullWidth />
                                    )}
                                    loading={loadingLists}
                                    loadingText="Загрузка…"
                                    getOptionDisabled={o => day1Projects.some(p => p.id === o.id)}
                                />
                            </Stack>

                            <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
                                <Button
                                    variant="contained"
                                    onClick={handleSave}
                                    disabled={saving || loadingLists}
                                    startIcon={saving ? <CircularProgress size={18} /> : null}
                                >
                                    {saving ? 'Сохранение…' : 'Сохранить'}
                                </Button>
                            </Box>
                        </Stack>
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

export default EventProjectDayAssignment;
