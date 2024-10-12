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
    const [projects, setProjects] = useState([]); // Список проектов
    const [selectedProject, setSelectedProject] = useState(null); // Выбранный проект
    const [events, setEvents] = useState([]); // Список событий для выбранного проекта
    const [selectedEvent, setSelectedEvent] = useState(null); // Выбранное событие
    const [willingJuries, setWillingJuries] = useState([]); // Жюри, желающие проверить
    const [obligedJuries, setObligedJuries] = useState([]); // Жюри, обязанные проверить
    const [mentors, setMentors] = useState([]); // Менторы
    const [juryOptions, setJuryOptions] = useState([]); // Доступные жюри
    const [applyToAllEvents, setApplyToAllEvents] = useState(false); // Галочка "Apply to all events"
    const [openSnackbar, setOpenSnackbar] = useState(false); // Состояние Snackbar
    const [snackbarMessage, setSnackbarMessage] = useState(''); // Сообщение Snackbar
    const [snackbarSeverity, setSnackbarSeverity] = useState('success'); // Тип Snackbar

    // Получаем проекты и жюри при монтировании компонента
    useEffect(() => {
        // Получаем проекты
        fetch('http://localhost:8080/api/v1/admin/projects')
            .then((response) => response.json())
            .then((data) => {
                setProjects(Array.isArray(data.content) ? data.content : []);
            })
            .catch((error) => console.error('Error fetching projects:', error));

        // Получаем жюри
        fetch('http://localhost:8080/api/v1/admin/users')
            .then((response) => response.json())
            .then((data) => {
                setJuryOptions(Array.isArray(data.content) ? data.content : []);
            })
            .catch((error) => console.error('Error fetching juries:', error));
    }, []);

    // Получаем события для выбранного проекта
    useEffect(() => {
        if (selectedProject && !applyToAllEvents) {
            fetch(`http://localhost:8080/api/v1/admin/projects/${selectedProject}/events`)
                .then((response) => response.json())
                .then((data) => {
                    setEvents(Array.isArray(data.content) ? data.content : []);
                })
                .catch((error) => console.error('Error fetching events:', error));
        } else {
            setEvents([]);
        }
        // Сбрасываем выбранное событие и жюри при изменении проекта
        setSelectedEvent(null);
        setWillingJuries([]);
        setObligedJuries([]);
        setMentors([]);
    }, [selectedProject, applyToAllEvents]);

    // Получаем назначенных жюри для выбранного проекта и события
    useEffect(() => {
        if (selectedProject && selectedEvent && !applyToAllEvents) {
            fetch(
                `http://localhost:8080/api/v1/admin/projects/${selectedProject}/juries/${selectedEvent}`
            )
                .then((response) => response.json())
                .then((data) => {
                    // Предполагаем, что бэк возвращает объект с массивами для каждой роли
                    setWillingJuries(data.willingJuries || []);
                    setObligedJuries(data.obligedJuries || []);
                    setMentors(data.mentors || []);
                })
                .catch((error) => console.error('Error fetching assigned juries:', error));
        } else {
            // Если выбрана галочка "Apply to all events", очищаем поля
            setWillingJuries([]);
            setObligedJuries([]);
            setMentors([]);
        }
    }, [selectedProject, selectedEvent, applyToAllEvents]);

    // Обработчики изменения выбранных жюри
    const handleWillingJuriesChange = (event, newValue) => {
        // Убираем жюри из других ролей
        setObligedJuries(obligedJuries.filter((jury) => !newValue.some((j) => j.id === jury.id)));
        setMentors(mentors.filter((jury) => !newValue.some((j) => j.id === jury.id)));
        setWillingJuries(newValue);
    };

    const handleObligedJuriesChange = (event, newValue) => {
        setWillingJuries(willingJuries.filter((jury) => !newValue.some((j) => j.id === jury.id)));
        setMentors(mentors.filter((jury) => !newValue.some((j) => j.id === jury.id)));
        setObligedJuries(newValue);
    };

    const handleMentorsChange = (event, newValue) => {
        setWillingJuries(willingJuries.filter((jury) => !newValue.some((j) => j.id === jury.id)));
        setObligedJuries(obligedJuries.filter((jury) => !newValue.some((j) => j.id === jury.id)));
        setMentors(newValue);
    };

    // Отправка изменений на бэк
    const handleSubmit = () => {
        const data = {
            projectId: selectedProject,
            eventId: applyToAllEvents ? null : selectedEvent, // null означает все события
            willingJuries: willingJuries.map((jury) => jury.id),
            obligedJuries: obligedJuries.map((jury) => jury.id),
            mentors: mentors.map((mentor) => mentor.id),
            applyToAllEvents: applyToAllEvents,
        };

        fetch(`http://localhost:8080/api/v1/admin/projects/${selectedProject}/juries`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        })
            .then((response) => {
                if (response.ok) {
                    setSnackbarMessage('Saved successfully');
                    setSnackbarSeverity('success');
                    setOpenSnackbar(true);
                } else {
                    setSnackbarMessage('Failed to save');
                    setSnackbarSeverity('error');
                    setOpenSnackbar(true);
                }
            })
            .catch((error) => {
                console.error('Error saving project juries:', error);
                setSnackbarMessage('Failed to save');
                setSnackbarSeverity('error');
                setOpenSnackbar(true);
            });
    };

    // Закрытие Snackbar
    const handleSnackbarClose = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenSnackbar(false);
    };

    // Фильтрация жюри, чтобы исключить уже выбранных в других ролях
    const getFilteredJuryOptions = (currentRole) => {
        const selectedIds = new Set([
            ...willingJuries.map((jury) => jury.id),
            ...obligedJuries.map((jury) => jury.id),
            ...mentors.map((jury) => jury.id),
        ]);

        // Убираем ID жюри, уже выбранных в текущей роли
        if (currentRole === 'willing') {
            willingJuries.forEach((jury) => selectedIds.delete(jury.id));
        } else if (currentRole === 'obliged') {
            obligedJuries.forEach((jury) => selectedIds.delete(jury.id));
        } else if (currentRole === 'mentor') {
            mentors.forEach((jury) => selectedIds.delete(jury.id));
        }

        return juryOptions.filter((jury) => !selectedIds.has(jury.id));
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
                    setApplyToAllEvents(false); // Сбрасываем галочку
                    setSelectedEvent(null); // Сбрасываем выбранное событие
                    setWillingJuries([]); // Очищаем жюри
                    setObligedJuries([]);
                    setMentors([]);
                }}
                renderInput={(params) => <TextField {...params} label="Select Project" />}
            />

            {/* Галочка "Apply to all events" */}
            {selectedProject && (
                <FormControlLabel
                    control={
                        <Checkbox
                            checked={applyToAllEvents}
                            onChange={(e) => {
                                setApplyToAllEvents(e.target.checked);
                                setSelectedEvent(null); // Сбрасываем выбранное событие
                                setWillingJuries([]); // Очищаем жюри
                                setObligedJuries([]);
                                setMentors([]);
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
                    getOptionLabel={(option) => option.name}
                    onChange={(event, newValue) => {
                        setSelectedEvent(newValue?.id || null);
                        // Очищаем жюри при изменении события
                        setWillingJuries([]);
                        setObligedJuries([]);
                        setMentors([]);
                    }}
                    renderInput={(params) => <TextField {...params} label="Select Event" />}
                />
            )}

            {/* Выбор жюри */}
            {selectedProject && ((selectedEvent && !applyToAllEvents) || applyToAllEvents) && (
                <>
                    {/* Жюри, желающие проверить */}
                    <Autocomplete
                        multiple
                        options={getFilteredJuryOptions('willing')}
                        getOptionLabel={(option) => `${option.firstName} ${option.lastName}`}
                        isOptionEqualToValue={(option, value) => option.id === value.id}
                        value={willingJuries}
                        onChange={handleWillingJuriesChange}
                        renderInput={(params) => <TextField {...params} label="Willing Juries" />}
                    />

                    {/* Жюри, обязанные проверить */}
                    <Autocomplete
                        multiple
                        options={getFilteredJuryOptions('obliged')}
                        getOptionLabel={(option) => `${option.firstName} ${option.lastName}`}
                        isOptionEqualToValue={(option, value) => option.id === value.id}
                        value={obligedJuries}
                        onChange={handleObligedJuriesChange}
                        renderInput={(params) => <TextField {...params} label="Obliged Juries" />}
                    />

                    {/* Менторы */}
                    <Autocomplete
                        multiple
                        options={getFilteredJuryOptions('mentor')}
                        getOptionLabel={(option) => `${option.firstName} ${option.lastName}`}
                        isOptionEqualToValue={(option, value) => option.id === value.id}
                        value={mentors}
                        onChange={handleMentorsChange}
                        renderInput={(params) => <TextField {...params} label="Mentors" />}
                    />

                    {/* Кнопка сохранения */}
                    <Button onClick={handleSubmit} variant="contained" color="primary">
                        Save Changes
                    </Button>
                </>
            )}

            {/* Snackbar для отображения сообщений */}
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
