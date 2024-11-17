import React, { useState, useEffect } from 'react';
import { Container, FormControl, InputLabel, Select, MenuItem, Button, List, ListItem, ListItemText } from '@mui/material';

const GradeTable = () => {
    const [year, setYear] = useState(''); // Устанавливаем пустое значение по умолчанию
    const [years, setYears] = useState([]); // Список годов, получаемый с бэка
    const [events, setEvents] = useState([]); // Список событий
    const [selectedEvent, setSelectedEvent] = useState('');
    const [grades, setGrades] = useState([]);
    const [isTableVisible, setIsTableVisible] = useState(false);

    // Получение доступных годов при загрузке компонента
    useEffect(() => {
        const fetchYears = async () => {
            try {
                const response = await fetch('http://localhost:8080/api/v1/public/events/years');
                if (!response.ok) throw new Error('Ошибка при загрузке годов');
                const data = await response.json();
                const yearsData = data.content; // Извлекаем годы из content

                setYears(yearsData);

                // Устанавливаем текущий год, если он есть в списке, или первый год из списка
                const currentYear = new Date().getFullYear();
                setYear(yearsData.includes(currentYear) ? currentYear : yearsData[0] || ''); // Устанавливаем значение после загрузки
            } catch (error) {
                console.error(error.message);
                setYears([]); // В случае ошибки устанавливаем years как пустой массив
            }
        };

        fetchYears();
    }, []);

    // Получение событий при изменении года
    useEffect(() => {
        const fetchEvents = async () => {
            try {
                const response = await fetch(`http://localhost:8080/api/v1/public/events?year=${year}`);
                if (!response.ok) throw new Error('Ошибка при загрузке событий');
                const data = await response.json();
                setEvents(Array.isArray(data.content) ? data.content : []);
                setSelectedEvent(''); // Сбрасываем выбранное событие при изменении года
            } catch (error) {
                console.error(error.message);
                setEvents([]); // В случае ошибки устанавливаем events как пустой массив
            }
        };

        if (year) fetchEvents();
    }, [year]);

    // Обработчик изменения года
    const handleYearChange = (event) => {
        setYear(event.target.value);
        setIsTableVisible(false); // Скрываем таблицу при изменении года
    };

    // Обработчик изменения события
    const handleEventChange = (event) => {
        setSelectedEvent(event.target.value);
        setIsTableVisible(false); // Скрываем таблицу при изменении события
        console.log("Selected Event UUID:", event.target.value); // Выводим uuid выбранного события
    };

    // Запрос на получение оценок для выбранного события
    const fetchGrades = async () => {
        if (!selectedEvent) return;

        try {
            const response = await fetch(`http://localhost:8080/api/v1/jury/grades?eventId=${selectedEvent}`);
            if (!response.ok) throw new Error('Ошибка при загрузке оценок');
            const data = await response.json();
            setGrades(Array.isArray(data) ? data : []);
            setIsTableVisible(true); // Показываем таблицу после получения данных
        } catch (error) {
            console.error(error.message);
            setGrades([]);
        }
    };

    return (
        <Container>
            <h2>Просмотр оценок по событию</h2>

            {/* Выбор года */}
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

            {/* Выбор события */}
            <FormControl fullWidth margin="normal" disabled={!events.length}>
                <InputLabel>Событие</InputLabel>
                <Select value={selectedEvent} onChange={handleEventChange}>
                    {events.map((event) => (
                        <MenuItem key={event.id} value={event.id}>
                            {event.name}
                        </MenuItem>
                    ))}
                </Select>
            </FormControl>

            {/* Кнопка для получения оценок */}
            <Button
                variant="contained"
                color="primary"
                onClick={fetchGrades}
                disabled={!selectedEvent}
                sx={{ marginTop: 2 }}
            >
                Показать оценки
            </Button>

            {/* Список оценок */}
            {isTableVisible && (
                <List sx={{ marginTop: 2 }}>
                    {grades.map((grade) => (
                        <ListItem key={`${grade.projectId}-${grade.juryId}`}>
                            <ListItemText
                                primary={`Проект: ${grade.projectName} - Жюри: ${grade.juryName}`}
                                secondary={`Презентация: ${grade.presPoints}, Сборка: ${grade.buildPoints}, Комментарий: ${grade.comment}`}
                            />
                        </ListItem>
                    ))}
                </List>
            )}
        </Container>
    );
};

export default GradeTable;
