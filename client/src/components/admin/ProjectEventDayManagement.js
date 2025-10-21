import React, { useEffect, useState } from 'react';
import {
    Autocomplete,
    Button,
    TextField,
    Stack,
    CircularProgress,
    Snackbar,
    Alert,
    Typography,
} from '@mui/material';
import { Title } from 'react-admin';

const EventProjectDayAssignment = () => {
    const [events, setEvents] = useState([]);
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [allProjects, setAllProjects] = useState([]);
    const [day1Projects, setDay1Projects] = useState([]);
    const [day2Projects, setDay2Projects] = useState([]);
    const [loading, setLoading] = useState(false);
    const [openSnackbar, setOpenSnackbar] = useState(false);
    const [snackbarMessage, setSnackbarMessage] = useState('');
    const [snackbarSeverity, setSnackbarSeverity] = useState('success');

    useEffect(() => {
        const fetchEvents = async () => {
            try {
                const res = await fetch('/api/v1/admin/events', { credentials: 'include' });
                const data = await res.json();
                setEvents(Array.isArray(data.content) ? data.content : []);
            } catch (err) {
                console.error('Error fetching events:', err);
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
                setLoading(true);
                const resAll = await fetch(
                    `/api/v1/admin/events/${selectedEvent}/projects`,
                    { credentials: 'include' }
                );
                const dataAll = await resAll.json();
                setAllProjects(Array.isArray(dataAll.content) ? dataAll.content : []);

                const resDays = await fetch(
                    `/api/v1/admin/events/${selectedEvent}/projects/days`,
                    { credentials: 'include' }
                );
                const dataDays = await resDays.json();
                const content = dataDays.content || {};
                setDay1Projects(Array.isArray(content.firstDayProjects) ? content.firstDayProjects : []);
                setDay2Projects(Array.isArray(content.secondDayProjects) ? content.secondDayProjects : []);
            } catch (err) {
                console.error('Error fetching data for event:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchAll();
    }, [selectedEvent]);

    const handleSubmit = async () => {
        if (!selectedEvent) return;
        setLoading(true);
        const payload = {
            firstDayProjects: day1Projects.map(p => p.id),
            secondDayProjects: day2Projects.map(p => p.id),
        };
        try {
            const response = await fetch(
                `/api/v1/admin/events/${selectedEvent}/days`,
                {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body: JSON.stringify(payload),
                }
            );
            if (response.ok) {
                setSnackbarMessage('Изменения сохранены');
                setSnackbarSeverity('success');
            } else {
                const errData = await response.json().catch(() => ({}));
                setSnackbarMessage(errData.error || 'Не удалось сохранить изменения');
                setSnackbarSeverity('error');
            }
        } catch (err) {
            console.error('Error saving assignments:', err);
            setSnackbarMessage('Сетевая ошибка: не удалось сохранить');
            setSnackbarSeverity('error');
        } finally {
            setLoading(false);
            setOpenSnackbar(true);
        }
    };

    const handleCloseSnackbar = (_, reason) => {
        if (reason === 'clickaway') return;
        setOpenSnackbar(false);
    };

    return (
        <>
            <Title title="Дни защиты" />
            <Typography variant="h5" sx={{ mb: 2 }}>Распределение проектов по дням защиты</Typography>

            <Stack spacing={3} sx={{ maxWidth: 720, width: '100%' }}>
                <Autocomplete
                    options={events}
                    fullWidth
                    getOptionLabel={opt => `${opt.name} ${opt.year}-${parseInt(opt.year) + 1}`}
                    onChange={(_, val) => setSelectedEvent(val?.id || null)}
                    renderInput={params => <TextField {...params} label="Выберите этап отчётности" />}
                />

                {loading && <CircularProgress />}

                {selectedEvent && !loading && (
                    <Stack spacing={3}>
                        <Stack spacing={1.5}>
                            <Typography variant="subtitle1">1-й день защиты</Typography>
                            <Autocomplete
                                multiple
                                fullWidth
                                options={allProjects}
                                getOptionLabel={opt => opt.name}
                                isOptionEqualToValue={(opt, val) => opt.id === val.id}
                                value={day1Projects}
                                onChange={(_, val) => setDay1Projects(val)}
                                renderInput={params => <TextField {...params} label="Проекты первого дня" />}
                                getOptionDisabled={opt => day2Projects.some(p => p.id === opt.id)}
                            />
                        </Stack>

                        <Stack spacing={1.5}>
                            <Typography variant="subtitle1">2-й день защиты</Typography>
                            <Autocomplete
                                multiple
                                fullWidth
                                options={allProjects}
                                getOptionLabel={opt => opt.name}
                                isOptionEqualToValue={(opt, val) => opt.id === val.id}
                                value={day2Projects}
                                onChange={(_, val) => setDay2Projects(val)}
                                renderInput={params => <TextField {...params} label="Проекты второго дня" />}
                                getOptionDisabled={opt => day1Projects.some(p => p.id === opt.id)}
                            />
                        </Stack>

                        <Stack direction="row" spacing={2} justifyContent="flex-end">
                            <Button
                                variant="contained"
                                onClick={handleSubmit}
                                disabled={loading}
                            >
                                {loading ? 'Сохранение…' : 'Сохранить'}
                            </Button>
                        </Stack>
                    </Stack>
                )}
            </Stack>

            <Snackbar
                open={openSnackbar}
                autoHideDuration={3000}
                onClose={handleCloseSnackbar}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
            >
                <Alert onClose={handleCloseSnackbar} severity={snackbarSeverity} sx={{ width: '100%' }}>
                    {snackbarMessage}
                </Alert>
            </Snackbar>
        </>
    );
};

export default EventProjectDayAssignment;
