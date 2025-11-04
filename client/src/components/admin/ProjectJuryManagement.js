// ProjectJuryManagement.js
import React, { useEffect, useState, useMemo } from 'react';
import {
    Alert,
    Autocomplete,
    Box,
    Button,
    Checkbox,
    FormControlLabel,
    Snackbar,
    Stack,
    TextField,
    Typography,
    Divider,
    CircularProgress,
} from '@mui/material';
import { Title } from 'react-admin';

const ProjectJuryManagement = () => {
    const [projects, setProjects] = useState([]);
    const [selectedProject, setSelectedProject] = useState(null);
    const [events, setEvents] = useState([]);
    const [selectedEvent, setSelectedEvent] = useState(null);

    const [juryOptions, setJuryOptions] = useState([]);
    const [willingJuries, setWillingJuries] = useState([]);
    const [obligedJuries, setObligedJuries] = useState([]);
    const [mentors, setMentors] = useState([]);

    const [applyToAllEvents, setApplyToAllEvents] = useState(false);

    const [openSnackbar, setOpenSnackbar] = useState(false);
    const [snackbarMessage, setSnackbarMessage] = useState('');
    const [snackbarSeverity, setSnackbarSeverity] = useState('success');

    const [loading, setLoading] = useState(false);
    const [loadingLists, setLoadingLists] = useState(false);

    useEffect(() => {
        const fetchInitialData = async () => {
            try {
                setLoadingLists(true);
                const [projectsRes, juriesRes] = await Promise.all([
                    fetch('/api/v1/admin/projects?sort=name&limit=100', { credentials: 'include' }),
                    fetch('/api/v1/admin/users?sort=lastName&limit=100', { credentials: 'include' }),
                ]);
                const projectsData = await projectsRes.json();
                const juriesData = await juriesRes.json();
                setProjects(Array.isArray(projectsData.content) ? projectsData.content : []);
                setJuryOptions(Array.isArray(juriesData.content) ? juriesData.content : []);
            } catch (e) {
                console.error('Init fetch error:', e);
            } finally {
                setLoadingLists(false);
            }
        };
        fetchInitialData();
    }, []);

    useEffect(() => {
        const fetchEvents = async () => {
            if (selectedProject && !applyToAllEvents) {
                try {
                    setLoadingLists(true);
                    const res = await fetch(`/api/v1/admin/projects/${selectedProject}/events?limit=100`, {
                        credentials: 'include',
                    });
                    const data = await res.json();
                    const newEvents = Array.isArray(data.content) ? data.content : [];
                    setEvents(newEvents);

                    if (!newEvents.some((ev) => ev.id === selectedEvent)) {
                        setSelectedEvent(null);
                        setWillingJuries([]);
                        setObligedJuries([]);
                        setMentors([]);
                    }
                } catch (e) {
                    console.error('Fetch events error:', e);
                } finally {
                    setLoadingLists(false);
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
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [selectedProject, applyToAllEvents]);

    useEffect(() => {
        const fetchAssigned = async () => {
            if (selectedProject && selectedEvent) {
                try {
                    setLoadingLists(true);
                    const res = await fetch(
                        `/api/v1/admin/projects/${selectedProject}/juries/${selectedEvent}`,
                        { credentials: 'include' }
                    );
                    const data = await res.json();
                    setWillingJuries(data.willingJuries || []);
                    setObligedJuries(data.obligedJuries || []);
                    setMentors(data.mentors || []);
                } catch (e) {
                    console.error('Fetch assigned juries error:', e);
                } finally {
                    setLoadingLists(false);
                }
            }
        };
        fetchAssigned();
    }, [selectedProject, selectedEvent]);

    const handleWillingJuriesChange = (_e, val) => {
        setObligedJuries((prev) => prev.filter((x) => !val.some((j) => j.id === x.id)));
        setMentors((prev) => prev.filter((x) => !val.some((j) => j.id === x.id)));
        setWillingJuries(val);
    };

    const handleObligedJuriesChange = (_e, val) => {
        setWillingJuries((prev) => prev.filter((x) => !val.some((j) => j.id === x.id)));
        setMentors((prev) => prev.filter((x) => !val.some((j) => j.id === x.id)));
        setObligedJuries(val);
    };

    const handleMentorsChange = (_e, val) => {
        setWillingJuries((prev) => prev.filter((x) => !val.some((j) => j.id === x.id)));
        setObligedJuries((prev) => prev.filter((x) => !val.some((j) => j.id === x.id)));
        setMentors(val);
    };

    const selectedIdsAll = useMemo(
        () => new Set([...willingJuries, ...obligedJuries, ...mentors].map((u) => u.id)),
        [willingJuries, obligedJuries, mentors]
    );

    const filteredOptions = (currentRole) => {
        const copy = new Set(selectedIdsAll);
        if (currentRole === 'willing') willingJuries.forEach((u) => copy.delete(u.id));
        if (currentRole === 'obliged') obligedJuries.forEach((u) => copy.delete(u.id));
        if (currentRole === 'mentor') mentors.forEach((u) => copy.delete(u.id));
        return juryOptions.filter((u) => !copy.has(u.id));
    };

    const handleSubmit = async () => {
        if (!selectedProject) return;
        setLoading(true);
        const payload = {
            projectId: selectedProject,
            eventId: applyToAllEvents ? null : selectedEvent,
            willingJuries: willingJuries.map((u) => u.id),
            obligedJuries: obligedJuries.map((u) => u.id),
            mentors: mentors.map((u) => u.id),
            applyToAllEvents,
        };

        try {
            const res = await fetch(`/api/v1/admin/projects/assign`, {
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
            console.error('Save error:', e);
            setSnackbarMessage('Во время сохранения произошла ошибка');
            setSnackbarSeverity('error');
        } finally {
            setOpenSnackbar(true);
            setLoading(false);
        }
    };

    const canSave =
        !!selectedProject && (applyToAllEvents || (!!selectedEvent && !loadingLists));

    return (
        <>
            <Title title="Проверяющие и менторы" />
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
                    Распределение жюри и менторов по проектам
                </Typography>
                <Divider />

                <Stack spacing={3}>
                    <Autocomplete
                        options={projects}
                        getOptionLabel={(o) =>
                            o?.name ? `${o.name} ${o.year}-${parseInt(o.year, 10) + 1}` : ''
                        }
                        onChange={(_e, v) => {
                            setSelectedProject(v?.id || null);
                            setApplyToAllEvents(false);
                            setSelectedEvent(null);
                            setWillingJuries([]);
                            setObligedJuries([]);
                            setMentors([]);
                        }}
                        renderInput={(params) => (
                            <TextField {...params} label="Выберите проект" size="small" fullWidth />
                        )}
                        loading={loadingLists}
                        loadingText="Загрузка…"
                    />

                    {selectedProject && (
                        <FormControlLabel
                            control={
                                <Checkbox
                                    checked={applyToAllEvents}
                                    onChange={(e) => {
                                        setApplyToAllEvents(e.target.checked);
                                        if (e.target.checked) {
                                            setSelectedEvent(null);
                                            setWillingJuries([]);
                                            setObligedJuries([]);
                                            setMentors([]);
                                        }
                                    }}
                                />
                            }
                            label="Установить для всех этапов отчётности"
                        />
                    )}

                    {!applyToAllEvents && selectedProject && (
                        <Autocomplete
                            options={events}
                            value={events.find((ev) => ev.id === selectedEvent) || null}
                            getOptionLabel={(o) => o?.name || ''}
                            onChange={(_e, v) => {
                                setSelectedEvent(v?.id || null);
                                setWillingJuries([]);
                                setObligedJuries([]);
                                setMentors([]);
                            }}
                            renderInput={(params) => (
                                <TextField
                                    {...params}
                                    label="Выберите этап отчётности"
                                    size="small"
                                    fullWidth
                                />
                            )}
                            loading={loadingLists}
                            loadingText="Загрузка…"
                        />
                    )}

                    {((selectedProject && applyToAllEvents) ||
                        (selectedProject && selectedEvent)) && (
                        <>
                            <Autocomplete
                                multiple
                                options={filteredOptions('willing')}
                                getOptionLabel={(o) =>
                                    o ? `${o.lastName} ${o.firstName}`.trim() : ''
                                }
                                isOptionEqualToValue={(o, v) => o.id === v.id}
                                value={willingJuries}
                                onChange={handleWillingJuriesChange}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        label="Желают проверить"
                                        size="small"
                                        fullWidth
                                    />
                                )}
                            />

                            <Autocomplete
                                multiple
                                options={filteredOptions('obliged')}
                                getOptionLabel={(o) =>
                                    o ? `${o.lastName} ${o.firstName}`.trim() : ''
                                }
                                isOptionEqualToValue={(o, v) => o.id === v.id}
                                value={obligedJuries}
                                onChange={handleObligedJuriesChange}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        label="Обязаны проверить"
                                        size="small"
                                        fullWidth
                                    />
                                )}
                            />

                            <Autocomplete
                                multiple
                                options={filteredOptions('mentor')}
                                getOptionLabel={(o) =>
                                    o ? `${o.lastName} ${o.firstName}`.trim() : ''
                                }
                                isOptionEqualToValue={(o, v) => o.id === v.id}
                                value={mentors}
                                onChange={handleMentorsChange}
                                renderInput={(params) => (
                                    <TextField {...params} label="Менторы" size="small" fullWidth />
                                )}
                            />

                            <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
                                <Button
                                    variant="contained"
                                    onClick={handleSubmit}
                                    disabled={loading || !canSave}
                                    startIcon={loading ? <CircularProgress size={18} /> : null}
                                >
                                    {loading ? 'Сохранение…' : 'Сохранить'}
                                </Button>
                            </Box>
                        </>
                    )}
                </Stack>
            </Box>

            <Snackbar
                open={openSnackbar}
                autoHideDuration={3000}
                onClose={(_, r) => r !== 'clickaway' && setOpenSnackbar(false)}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
            >
                <Alert
                    onClose={() => setOpenSnackbar(false)}
                    severity={snackbarSeverity}
                    sx={{ width: '100%' }}
                >
                    {snackbarMessage}
                </Alert>
            </Snackbar>
        </>
    );
};

export default ProjectJuryManagement;
