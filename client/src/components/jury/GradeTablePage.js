// client/src/components/jury/GradeTablePage.jsx
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
    Typography,
    Paper,
    Fade,
    Slide,
} from '@mui/material';
import InfoIcon from '@mui/icons-material/Info';
import GradeTable from './GradeTable';

const APPBAR_H = 64; // высота глобальной шапки

const GradeTablePage = () => {
    const [year, setYear] = useState('');
    const [years, setYears] = useState([]);
    const [events, setEvents] = useState([]);
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [grades, setGrades] = useState(null);
    const [loading, setLoading] = useState(false);
    const [showOnlyMy, setShowOnlyMy] = useState(false);
    const [showMentored, setShowMentored] = useState(false);
    const [selectedDay, setSelectedDay] = useState('all'); // 'all', '1', '2'

    // локальный флаг для плавного появления страницы
    const [mounted, setMounted] = useState(false);
    useEffect(() => {
        const t = requestAnimationFrame(() => setMounted(true));
        return () => cancelAnimationFrame(t);
    }, []);

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
                setGrades(null);
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
        if (selectedDay !== 'all') url += `&day=${selectedDay}`;

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
        <Fade in={mounted} timeout={220}>
            <Container maxWidth="xl" sx={{ mt: 2 }}>
                <Box sx={{ mb: 2 }}>
                    <Typography variant="h5" sx={{ fontWeight: 700, mb: 0.5 }}>
                        Просмотр оценок по событию
                    </Typography>
                    <Typography variant="body2" sx={{ opacity: 0.8 }}>
                        Выберите год и событие, при необходимости отфильтруйте по дню защиты.
                    </Typography>
                </Box>

                <Grid container spacing={2} alignItems="flex-start">
                    {/* ЛЕВАЯ КОЛОНКА — ФИЛЬТРЫ */}
                    <Grid item xs={12} md={3} lg={3}>
                        <Slide in={mounted} direction="right" timeout={240}>
                            <Paper
                                elevation={0}
                                sx={{
                                    p: 2,
                                    border: (t) => `1px solid ${t.palette.divider}`,
                                    borderRadius: 2,
                                    position: 'sticky',
                                    top: APPBAR_H + 16, // под шапкой и с небольшим отступом
                                }}
                            >
                                <FormControl fullWidth margin="normal">
                                    <InputLabel>Год</InputLabel>
                                    <Select value={year} onChange={handleYearChange} label="Год">
                                        {years.map(y => <MenuItem key={y} value={y}>{y}</MenuItem>)}
                                    </Select>
                                </FormControl>

                                <FormControl fullWidth margin="normal" disabled={!events.length}>
                                    <InputLabel>Событие</InputLabel>
                                    <Select value={selectedEvent || ''} onChange={handleEventChange} label="Событие">
                                        {events.map(ev => <MenuItem key={ev.id} value={ev}>{ev.name}</MenuItem>)}
                                    </Select>
                                </FormControl>

                                <FormControl component="fieldset" sx={{ mt: 2, mb: 1 }}>
                                    <Typography variant="subtitle2" sx={{ fontWeight: 600, mb: 0.5 }}>
                                        День защиты
                                    </Typography>
                                    <RadioGroup value={selectedDay} onChange={(e) => setSelectedDay(e.target.value)}>
                                        <FormControlLabel value="all" control={<Radio size="small" />} label="Все дни" />
                                        <FormControlLabel value="1" control={<Radio size="small" />} label="День 1" />
                                        <FormControlLabel value="2" control={<Radio size="small" />} label="День 2" />
                                    </RadioGroup>
                                </FormControl>

                                <FormGroup sx={{ mt: 1 }}>
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
                                            <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                                <span>Только мои</span>
                                                <Tooltip title="Показать проекты, где вы назначены проверяющим" arrow>
                                                    <IconButton size="small" sx={{ p: 0.5, ml: 0.5 }}>
                                                        <InfoIcon fontSize="inherit" />
                                                    </IconButton>
                                                </Tooltip>
                                            </Box>
                                        }
                                    />
                                    {showOnlyMy && (
                                        <FormControlLabel
                                            control={<Switch checked={showMentored} onChange={() => setShowMentored(!showMentored)} />}
                                            label={
                                                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                                    <span>Под моим менторством</span>
                                                    <Tooltip title="Показать только проекты, где вы являетесь ментором" arrow>
                                                        <IconButton size="small" sx={{ p: 0.5, ml: 0.5 }}>
                                                            <InfoIcon fontSize="inherit" />
                                                        </IconButton>
                                                    </Tooltip>
                                                </Box>
                                            }
                                        />
                                    )}
                                </FormGroup>

                                <Button
                                    fullWidth
                                    variant="contained"
                                    color="primary"
                                    onClick={fetchGrades}
                                    disabled={!selectedEvent || loading}
                                    sx={{ mt: 2 }}
                                >
                                    {loading ? 'Загрузка…' : 'Показать оценки'}
                                </Button>
                            </Paper>
                        </Slide>
                    </Grid>

                    {/* ПРАВАЯ КОЛОНКА — ТАБЛИЦА */}
                    <Grid item xs={12} md={9} lg={9}>
                        <Fade in={mounted} timeout={240}>
                            <Box sx={{ width: '100%', overflowX: 'auto', mb: 4, pb: 4 }}>
                                {!grades && (
                                    <Box sx={{ p: 2, opacity: 0.8 }}>
                                        Здесь появятся оценки после выбора события.
                                    </Box>
                                )}

                                {grades && showOnlyMy && showMentored && grades.projects?.length === 0 && (
                                    <Box sx={{ p: 2, opacity: 0.8 }}>
                                        На данном этапе отчётности вы не являетесь ментором ни одного проекта.
                                    </Box>
                                )}

                                {grades && !(showOnlyMy && showMentored && grades.projects?.length === 0) && (
                                    <GradeTable
                                        grades={grades}
                                        event={selectedEvent}
                                        onGradeUpdated={handleGradeUpdatedInParent}
                                    />
                                )}
                            </Box>
                        </Fade>
                    </Grid>
                </Grid>
            </Container>
        </Fade>
    );
};

export default GradeTablePage;
