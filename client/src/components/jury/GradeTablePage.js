import React, { useEffect, useState } from 'react';
import {
    Box,
    Button,
    Container,
    FormControl,
    FormControlLabel,
    Grid,
    InputLabel,
    MenuItem,
    Select,
    Switch,
    FormGroup,
    Tooltip,
    IconButton,
    Radio,
    RadioGroup,
    Typography
} from '@mui/material';
import GradeTable from './GradeTable';
import NavigationBar from '../home/NavigationBar';
import InfoIcon from '@mui/icons-material/Info';

const GradeTablePage = ({ isAuthenticated, setIsAuthenticated }) => {
    const [year, setYear] = useState('');
    const [years, setYears] = useState([]);
    const [events, setEvents] = useState([]);
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [grades, setGrades] = useState(null);
    const [loading, setLoading] = useState(false);
    const [showOnlyMy, setShowOnlyMy] = useState(false);
    const [showMentored, setShowMentored] = useState(false);
    const [selectedDay, setSelectedDay] = useState('all'); // 'all', '1', '2'

    const handleGradeUpdatedInParent = (updatedGrade) => {
        setGrades(prev => {
            const newContent = { ...prev };
            newContent.rows = newContent.rows.map(row => {
                if (row.projectId !== updatedGrade.projectId) return row;
                const newTableRow = [...row.tableRow];
                const idx = newTableRow.findIndex(g => g.juryId === updatedGrade.juryId);
                if (idx >= 0) newTableRow[idx] = updatedGrade;
                else newTableRow.push(updatedGrade);
                return { ...row, tableRow: newTableRow };
            });
            return newContent;
        });
    };

    useEffect(() => {
        const fetchYears = async () => {
            try {
                const response = await fetch('/api/v1/public/events/years', { credentials: 'include' });
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
                const response = await fetch(`/api/v1/public/events?year=${year}`, { credentials: 'include' });
                if (!response.ok) throw new Error('Ошибка при загрузке событий');
                const data = await response.json();
                setEvents(Array.isArray(data.content) ? data.content : []);
                setSelectedEvent(null);
            } catch (error) {
                console.error(error.message);
                setEvents([]);
            }
        };
        fetchEvents();
    }, [year]);

    const handleYearChange = (e) => setYear(e.target.value);
    const handleEventChange = (e) => setSelectedEvent(e.target.value);

    const fetchGrades = async () => {
        if (!selectedEvent) return;
        setLoading(true);
        let showParam = 'ALL';
        if (showOnlyMy) showParam = showMentored ? 'MENTORED' : 'ASSIGNED';

        let url = `/api/v1/jury/table/${selectedEvent.id}?show=${showParam}`;
        if (selectedDay !== 'all') {
            url += `&day=${selectedDay}`;
        }

        try {
            const response = await fetch(url, { credentials: 'include' });
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
            <NavigationBar isAuthenticated={isAuthenticated} setIsAuthenticated={setIsAuthenticated} />
            <Container maxWidth="xl" sx={{ mt: 4 }}>
                <Box sx={{ mb: 2 }}><h2>Просмотр оценок по событию</h2></Box>
                <Grid container spacing={2}>
                    <Grid item xs={12} sm={3} md={2}>
                        <Box sx={{ p: 2, border: '1px solid #ccc', borderRadius: 2 }}>
                            <FormControl fullWidth margin="normal">
                                <InputLabel>Год</InputLabel>
                                <Select value={year} onChange={handleYearChange} displayEmpty>
                                    {years.map(y => <MenuItem key={y} value={y}>{y}</MenuItem>)}
                                </Select>
                            </FormControl>
                            <FormControl fullWidth margin="normal" disabled={!events.length}>
                                <InputLabel>Событие</InputLabel>
                                <Select value={selectedEvent || ''} onChange={handleEventChange}>
                                    {events.map(ev => <MenuItem key={ev.id} value={ev}>{ev.name}</MenuItem>)}
                                </Select>
                            </FormControl>

                            <FormControl component="fieldset" sx={{ mt: 2, mb: 1 }}>
                                <Typography variant="subtitle1">День защиты:</Typography>
                                <RadioGroup
                                    value={selectedDay}
                                    onChange={(e) => setSelectedDay(e.target.value)}
                                >
                                    <FormControlLabel
                                        value="all"
                                        control={<Radio size="small" />}
                                        label="Все дни"
                                    />
                                    <FormControlLabel
                                        value="1"
                                        control={<Radio size="small" />}
                                        label="День 1"
                                    />
                                    <FormControlLabel
                                        value="2"
                                        control={<Radio size="small" />}
                                        label="День 2"
                                    />
                                </RadioGroup>
                            </FormControl>

                            <FormGroup>
                                <FormControlLabel
                                    control={
                                        <Switch
                                            checked={showOnlyMy}
                                            onChange={() => {
                                                setShowOnlyMy(!showOnlyMy);
                                                if (showOnlyMy) setShowMentored(false);
                                            }}
                                        />
                                    }
                                    label={
                                        <div style={{ display: 'flex', alignItems: 'center' }}>
                                            <span>Отобразить только мои</span>
                                            <Tooltip
                                                title="Отобразить только те проекты, для которых вы назначены проверяющим"
                                                arrow
                                            >
                                                <IconButton size="medium" style={{ padding: 0, marginLeft: 4 }}>
                                                    <InfoIcon fontSize="medium" />
                                                </IconButton>
                                            </Tooltip>
                                        </div>
                                    }
                                />
                                {showOnlyMy && (
                                    <FormControlLabel
                                        control={
                                            <Switch checked={showMentored} onChange={() => setShowMentored(!showMentored)} />
                                        }
                                        label={
                                            <div style={{ display: 'flex', alignItems: 'center' }}>
                                                <span>Под моим менторством</span>
                                                <Tooltip
                                                    title="Отобразить только те проекты, для которых вы являетесь ментором"
                                                    arrow
                                                >
                                                    <IconButton size="medium" style={{ padding: 0, marginLeft: 4 }}>
                                                        <InfoIcon fontSize="medium" />
                                                    </IconButton>
                                                </Tooltip>
                                            </div>
                                        }
                                    />
                                )}
                            </FormGroup>
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
                    <Grid item xs={12} sm={9} md={10}>
                        <Box sx={{ overflowX: 'auto', width: '100%', mb: 4, pb: 4 }}>
                            {!grades && (
                                <Box sx={{ p: 2 }}>
                                    Здесь появятся оценки после выбора события.
                                </Box>
                            )}
                            {grades && showOnlyMy && showMentored && grades.projects.length === 0 && (
                                <Box sx={{ p: 2 }}>
                                    К сожалению (или к счастью?), на данном этапе отчётности вы не являетесь ментором ни одного проекта.
                                </Box>
                            )}
                            {grades && !(showOnlyMy && showMentored && grades.projects.length === 0) && (
                                <GradeTable
                                    grades={grades}
                                    event={selectedEvent}
                                    onGradeUpdated={handleGradeUpdatedInParent}
                                />
                            )}
                        </Box>
                    </Grid>
                </Grid>
            </Container>
        </>
    );
};

export default GradeTablePage;