import React, { useEffect, useState } from 'react';
import { Autocomplete, TextField, Button, Checkbox, FormControlLabel } from '@mui/material';

const ProjectManagement = () => {
    const [projects, setProjects] = useState([]);  // Список проектов текущего года
    const [selectedProject, setSelectedProject] = useState(null); // Выбранный проект
    const [events, setEvents] = useState([]);  // Список эвентов
    const [selectedEvent, setSelectedEvent] = useState(null); // Выбранный эвент
    const [willingJuries, setWillingJuries] = useState([]);  // Жюри по желанию
    const [obligedJuries, setObligedJuries] = useState([]);  // Обязательные жюри
    const [mentors, setMentors] = useState([]);  // Менторы
    const [juryOptions, setJuryOptions] = useState([]);  // Доступные жюри
    const [applyToAllEvents, setApplyToAllEvents] = useState(false);  // Галочка "Все эвенты"

    // Загрузка данных с бэка (список проектов текущего года, эвенты и жюри)
    useEffect(() => {
        // Fetch all available projects (только проекты текущего года)
        fetch('http://localhost:8080/api/v1/admin/projects')
            .then((response) => response.json())
            .then((data) => {
                console.log('Projects data:', data);
                setProjects(Array.isArray(data.content) ? data.content : []); // Используем поле content
            })
            .catch((error) => console.error('Error fetching projects:', error));

        // Fetch all available juries
        fetch('http://localhost:8080/api/v1/admin/users')
            .then((response) => response.json())
            .then((data) => {
                console.log('Juries data:', data);
                setJuryOptions(Array.isArray(data.content) ? data.content : []);
            })
            .catch((error) => console.error('Error fetching juries:', error));
    }, []);

    // Загрузка эвентов для выбранного проекта
    useEffect(() => {
        if (selectedProject && !applyToAllEvents) {
            // Fetch events for selected project
            fetch(`http://localhost:8080/api/v1/admin/projects/${selectedProject}/events`)
                .then((response) => response.json())
                .then((data) => {
                    console.log('Events data:', data);
                    setEvents(Array.isArray(data.content) ? data.content : []);
                })
                .catch((error) => console.error('Error fetching events:', error));
        }
    }, [selectedProject, applyToAllEvents]);

    // Отправка изменений на бэк
    const handleSubmit = () => {
        const data = {
            projectId: selectedProject,
            eventId: applyToAllEvents ? null : selectedEvent, // Если выбраны все эвенты, eventId = null
            willingJuries: willingJuries.map(jury => jury.id),
            obligedJuries: obligedJuries.map(jury => jury.id),
            mentors: mentors.map(mentor => mentor.id),
            applyToAllEvents: applyToAllEvents,  // Признак применения к всем эвентам
        };

        fetch('/api/saveProjectJuries', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        })
            .then((response) => {
                if (response.ok) {
                    alert('Saved successfully');
                } else {
                    alert('Failed to save');
                }
            })
            .catch((error) => {
                console.error('Error saving project juries:', error);
                alert('Failed to save');
            });
    };

    return (
        <div>
            <h2>Manage Project Juries</h2>

            {/* Выбор проекта */}
            <Autocomplete
                options={projects}
                getOptionLabel={(option) => option.name}
                onChange={(event, newValue) => setSelectedProject(newValue?.id || null)}
                renderInput={(params) => <TextField {...params} label="Select Project" />}
            />

            {/* Галочка "Все эвенты" */}
            <FormControlLabel
                control={
                    <Checkbox
                        checked={applyToAllEvents}
                        onChange={(e) => setApplyToAllEvents(e.target.checked)}
                    />
                }
                label="Apply to all events"
            />

            {/* Выбор эвента (если не выбрано "Все эвенты") */}
            {!applyToAllEvents && (
                <Autocomplete
                    options={events}
                    getOptionLabel={(option) => option.name}
                    onChange={(event, newValue) => setSelectedEvent(newValue?.id || null)}
                    renderInput={(params) => <TextField {...params} label="Select Event" />}
                />
            )}

            {/* Список "проверяющие по желанию" */}
            <Autocomplete
                multiple
                options={juryOptions}
                getOptionLabel={(option) => option.firstName + " " + option.lastName}
                value={willingJuries}
                onChange={(event, newValue) => setWillingJuries(newValue)}
                renderInput={(params) => <TextField {...params} label="Willing Juries" />}
            />

            {/* Список "проверяющие обязательно" */}
            <Autocomplete
                multiple
                options={juryOptions}
                getOptionLabel={(option) => option.firstName + " " + option.lastName}
                value={obligedJuries}
                onChange={(event, newValue) => setObligedJuries(newValue)}
                renderInput={(params) => <TextField {...params} label="Obliged Juries" />}
            />

            {/* Список "Менторы" */}
            <Autocomplete
                multiple
                options={juryOptions}
                getOptionLabel={(option) => option.firstName + " " + option.lastName}
                value={mentors}
                onChange={(event, newValue) => setMentors(newValue)}
                renderInput={(params) => <TextField {...params} label="Mentors" />}
            />

            {/* Кнопка для сохранения изменений */}
            <Button onClick={handleSubmit} variant="contained" color="primary">
                Save Changes
            </Button>
        </div>
    );
};

export default ProjectManagement;
