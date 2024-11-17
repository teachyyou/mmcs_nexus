import React, { useState, useEffect } from 'react';
import { Container, Grid, FormControl, InputLabel, Select, MenuItem, Button } from '@mui/material';
import GradeTable from './GradeTable';

const GradeTablePage = () => {
    const [year, setYear] = useState('');
    const [years, setYears] = useState([]);
    const [events, setEvents] = useState([]);
    const [selectedEvent, setSelectedEvent] = useState('');
    const [grades, setGrades] = useState(null);

    useEffect(() => {
        const fetchYears = async () => {
            try {
                const response = await fetch('http://localhost:8080/api/v1/public/events/years');
                if (!response.ok) throw new Error('Ошибка при загрузке годов');
                const data = await response.json();
                const yearsData = data.content;
                setYears(yearsData);
                const currentYear = new Date().getFullYear();
                setYear(yearsData.includes(currentYear) ? currentYear : yearsData[0] || '');
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
                const response = await fetch(`http://localhost:8080/api/v1/public/events?year=${year}`);
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

    const handleYearChange = (event) => {
        setYear(event.target.value);
    };

    const handleEventChange = (event) => {
        setSelectedEvent(event.target.value);
    };

    const fetchGrades = async () => {
        if (!selectedEvent) return;

        try {
            const response = await fetch(`http://localhost:8080/api/v1/jury/table/${selectedEvent}`);
            if (!response.ok) throw new Error('Ошибка при загрузке оценок');
            const data = await response.json();
            setGrades(data.content);
        } catch (error) {
            console.error(error.message);
            setGrades(null);
        }
    };

    return (
        <Container>
            <h2>Просмотр оценок по событию</h2>
            <Grid container spacing={2}>
                {/* Панель выбора года и события слева, сужена */}
                <Grid item xs={2} sx={{ marginLeft: -2 }}>
                    <FormControl fullWidth margin="normal" sx={{ maxWidth: '90%' }}>
                        <InputLabel>Год</InputLabel>
                        <Select value={year} onChange={handleYearChange} displayEmpty>
                            {years.map((availableYear) => (
                                <MenuItem key={availableYear} value={availableYear}>
                                    {availableYear}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>

                    <FormControl fullWidth margin="normal" disabled={!events.length} sx={{ maxWidth: '90%' }}>
                        <InputLabel>Событие</InputLabel>
                        <Select value={selectedEvent} onChange={handleEventChange}>
                            {events.map((event) => (
                                <MenuItem key={event.id} value={event.id}>
                                    {event.name}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>

                    <Button
                        variant="contained"
                        color="primary"
                        onClick={fetchGrades}
                        disabled={!selectedEvent}
                        sx={{ marginTop: 2, maxWidth: '90%' }}
                    >
                        Показать оценки
                    </Button>
                </Grid>

                <Grid item xs={10}>
                    {grades && <GradeTable grades={grades} />}
                </Grid>
            </Grid>
        </Container>
    );
};

export default GradeTablePage;
