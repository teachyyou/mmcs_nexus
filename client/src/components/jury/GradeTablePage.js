import React, { useState, useEffect } from 'react';
import {
    Container,
    Grid,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    Button,
    Box
} from '@mui/material';
import GradeTable from './GradeTable';
import NavigationBar from '../home/NavigationBar';

const GradeTablePage = ({ isAuthenticated, setIsAuthenticated }) => {
    const [year, setYear] = useState('');
    const [years, setYears] = useState([]);
    const [events, setEvents] = useState([]);
    const [selectedEvent, setSelectedEvent] = useState('');
    const [grades, setGrades] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const fetchYears = async () => {
            try {
                const response = await fetch('http://localhost:8080/api/v1/public/events/years', {
                    credentials: 'include'
                });
                if (!response.ok) throw new Error('Ошибка при загрузке годов');
                const data = await response.json();
                const yearsData = data.content || [];
                setYears(yearsData);
                const currentYear = new Date().getFullYear();
                setYear(yearsData.includes(currentYear) ? currentYear : (yearsData[0] || ''));
            } catch (error) {
                console.error(error.message);
                setYears([]);
            }
        };
        fetchYears();
    }, []);

    useEffect(() => {
        const fetchEvents = async () => {
            if (!year) return;
            try {
                const response = await fetch(`http://localhost:8080/api/v1/public/events?year=${year}`, {
                    credentials: 'include'
                });
                if (!response.ok) throw new Error('Ошибка при загрузке событий');
                const data = await response.json();
                setEvents(Array.isArray(data.content) ? data.content : []);
                setSelectedEvent('');
            } catch (error) {
                console.error(error.message);
                setEvents([]);
            }
        };
        fetchEvents();
    }, [year]);

    const handleYearChange = (e) => {
        setYear(e.target.value);
    };

    const handleEventChange = (e) => {
        setSelectedEvent(e.target.value);
    };

    const fetchGrades = async () => {
        if (!selectedEvent) return;
        setLoading(true);
        try {
            const response = await fetch(`http://localhost:8080/api/v1/jury/table/${selectedEvent}`, {
                credentials: 'include',
            });
            if (!response.ok) throw new Error('Ошибка при загрузке оценок');
            const data = await response.json();
            setGrades(data.content);
        } catch (error) {
            console.error(error.message);
            setGrades(null);
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            {/* Навигационная панель сверху */}
            <NavigationBar isAuthenticated={isAuthenticated} setIsAuthenticated={setIsAuthenticated} />

            <Container maxWidth="xl" sx={{ mt: 4 }}>
                <Box sx={{ mb: 2 }}>
                    <h2>Просмотр оценок по событию</h2>
                </Box>
                <Grid container spacing={2}>
                    {/* Левая панель с выбором года и события */}
                    <Grid item xs={12} sm={3} md={2}>
                        <Box sx={{ p: 2, border: '1px solid #ccc', borderRadius: 2 }}>
                            <FormControl fullWidth margin="normal">
                                <InputLabel>Год</InputLabel>
                                <Select value={year} onChange={handleYearChange} displayEmpty>
                                    {years.map((availableYear) => (
                                        <MenuItem key={availableYear} value={availableYear}>
                                            {availableYear}
                                        </MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                            <FormControl fullWidth margin="normal" disabled={!events.length}>
                                <InputLabel>Событие</InputLabel>
                                <Select value={selectedEvent} onChange={handleEventChange}>
                                    {events.map((eventItem) => (
                                        <MenuItem key={eventItem.id} value={eventItem.id}>
                                            {eventItem.name}
                                        </MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                            <Button
                                variant="contained"
                                color="primary"
                                onClick={fetchGrades}
                                disabled={!selectedEvent || loading}
                                sx={{ mt: 2, width: '100%' }}
                            >
                                {loading ? 'Загрузка...' : 'Показать оценки'}
                            </Button>
                        </Box>
                    </Grid>
                    {/* Правая панель с таблицей оценок */}
                    <Grid item xs={12} sm={9} md={10}>
                        {grades ? (
                            <GradeTable grades={grades} />
                        ) : (
                            <Box sx={{ p: 2 }}>Здесь появятся оценки после выбора события.</Box>
                        )}
                    </Grid>
                </Grid>
            </Container>
        </>
    );
};

export default GradeTablePage;
