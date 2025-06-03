import React, { useEffect, useState } from 'react';
import { Autocomplete, Button, TextField, Stack, CircularProgress, Snackbar, Alert } from '@mui/material';
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

    // Fetch events list
    useEffect(() => {
        const fetchEvents = async () => {
            try {
                const res = await fetch('http://localhost:8080/api/v1/admin/events', { credentials: 'include' });
                const data = await res.json();
                setEvents(Array.isArray(data.content) ? data.content : []);
            } catch (err) {
                console.error('Error fetching events:', err);
            }
        };
        fetchEvents();
    }, []);

    // When event selected, fetch projects and assignments
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
                // all projects for event
                const resAll = await fetch(
                    `http://localhost:8080/api/v1/admin/events/${selectedEvent}/projects`,
                    { credentials: 'include' }
                );
                const dataAll = await resAll.json();
                setAllProjects(Array.isArray(dataAll.content) ? dataAll.content : []);

                // assigned by day
                const resDays = await fetch(
                    `http://localhost:8080/api/v1/admin/events/${selectedEvent}/projects/days`,
                    { credentials: 'include' }
                );
                const dataDays = await resDays.json();
                const content = dataDays.content || {};
                setDay1Projects(
                    Array.isArray(content.firstDayProjects) ? content.firstDayProjects : []
                );
                setDay2Projects(
                    Array.isArray(content.secondDayProjects) ? content.secondDayProjects : []
                );
            } catch (err) {
                console.error('Error fetching data for event:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchAll();
    }, [selectedEvent]);

    // Save handler with POST
    const handleSubmit = async () => {
        if (!selectedEvent) return;
        setLoading(true);
        const payload = {
            firstDayProjects: day1Projects.map(p => p.id),
            secondDayProjects: day2Projects.map(p => p.id)
        };
        try {
            const response = await fetch(
                `http://localhost:8080/api/v1/admin/events/${selectedEvent}/days`,
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    credentials: 'include',
                    body: JSON.stringify(payload)
                }
            );
            if (response.ok) {
                setSnackbarMessage('Assignments saved successfully');
                setSnackbarSeverity('success');
            } else {
                const errData = await response.json();
                setSnackbarMessage(errData.error || 'Failed to save assignments');
                setSnackbarSeverity('error');
            }
        } catch (err) {
            console.error('Error saving assignments:', err);
            setSnackbarMessage('Network error: could not save');
            setSnackbarSeverity('error');
        } finally {
            setLoading(false);
            setOpenSnackbar(true);
        }
    };

    const handleCloseSnackbar = (event, reason) => {
        if (reason === 'clickaway') return;
        setOpenSnackbar(false);
    };

    return (
        <>
            <Title title="Дни защиты" />
            <h2>Распределение проектов по дням защиты</h2>
            <Stack spacing={3} sx={{ width: 600 }}>
                <Autocomplete
                    options={events}
                    getOptionLabel={opt => `${opt.name} ${opt.year}-${parseInt(opt.year) + 1}`}
                    onChange={(e, val) => setSelectedEvent(val?.id || null)}
                    renderInput={params => <TextField {...params} label="Выберите этап отчётности" />}
                />

                {loading && <CircularProgress />}

                {selectedEvent && !loading && (
                    <>
                        <div>
                            <h4>1-ый день защиты</h4>
                            <Autocomplete
                                multiple
                                options={allProjects}
                                getOptionLabel={opt => opt.name}
                                isOptionEqualToValue={(opt, val) => opt.id === val.id}
                                value={day1Projects}
                                onChange={(e, val) => setDay1Projects(val)}
                                renderInput={params => <TextField {...params} label="Список проектов, назначенных на первый день" />}
                                getOptionDisabled={opt => day2Projects.some(p => p.id === opt.id)}
                                sx={{ width: '100%' }}
                            />
                        </div>

                        <div>
                            <h4>2-ой день защиты</h4>
                            <Autocomplete
                                multiple
                                options={allProjects}
                                getOptionLabel={opt => opt.name}
                                isOptionEqualToValue={(opt, val) => opt.id === val.id}
                                value={day2Projects}
                                onChange={(e, val) => setDay2Projects(val)}
                                renderInput={params => <TextField {...params} label="Список проектов, назначенных на второй день" />}
                                getOptionDisabled={opt => day1Projects.some(p => p.id === opt.id)}
                                sx={{ width: '100%' }}
                            />
                        </div>

                        <Button variant="contained" color="primary" onClick={handleSubmit} disabled={loading}>
                            {loading ? 'Сохранение...' : 'Сохранить'}
                        </Button>
                    </>
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
