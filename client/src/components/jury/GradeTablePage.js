// client/src/components/jury/GradeTablePage.jsx
import React, { useEffect, useState, useRef } from 'react';
import {
    Box,
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
import {Placeholder} from "react-admin";

const APPBAR_H = 64;

const GradeTablePage = () => {
    const [year, setYear] = useState('');
    const [years, setYears] = useState([]);
    const [events, setEvents] = useState([]);
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [grades, setGrades] = useState(null);
    const [loading, setLoading] = useState(false);
    const [showOnlyMy, setShowOnlyMy] = useState(false);
    const [showMentored, setShowMentored] = useState(false);
    const [selectedDay, setSelectedDay] = useState('all'); // 'all' | '1' | '2'

    // плавное появление страницы
    const [mounted, setMounted] = useState(false);
    useEffect(() => {
        const t = requestAnimationFrame(() => setMounted(true));
        return () => cancelAnimationFrame(t);
    }, []);

    const handleGradeUpdatedInParent = (updatedGrade) => {
        setGrades(prev => {
            if (!prev) return prev;
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

    // подгружаем список годов
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

    // подгружаем события по выбранному году
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

    // ===== РЕАКТИВНАЯ ПОДГРУЗКА ОЦЕНОК =====
    const fetchAbortRef = useRef(null);
    const debounceRef = useRef(null);

    useEffect(() => {
        // нет выбранного события — чистим и выходим
        if (!selectedEvent?.id) {
            setGrades(null);
            return;
        }

        // построим параметры показа
        let showParam = 'ALL';
        if (showOnlyMy) showParam = showMentored ? 'MENTORED' : 'ASSIGNED';

        // подготавливаем URL
        let url = `/api/v1/jury/grades/table/${selectedEvent.id}?show=${showParam}`;
        if (selectedDay !== 'all') url += `&day=${selectedDay}`;

        // дебаунс, чтобы не дергать API на каждый тик
        if (debounceRef.current) clearTimeout(debounceRef.current);
        debounceRef.current = setTimeout(() => {
            // отменяем предыдущий запрос, если есть
            if (fetchAbortRef.current) fetchAbortRef.current.abort();
            const controller = new AbortController();
            fetchAbortRef.current = controller;

            const run = async () => {
                setLoading(true);
                try {
                    const res = await fetch(url, { credentials: 'include', signal: controller.signal });
                    if (!res.ok) throw new Error('Ошибка при загрузке оценок');
                    const data = await res.json();
                    setGrades(data.content);
                } catch (err) {
                    if (err.name !== 'AbortError') {
                        console.error(err.message || err);
                        setGrades(null);
                    }
                } finally {
                    setLoading(false);
                }
            };

            run();
        }, 200); // 200мс достаточно комфортно

        // очистка
        return () => {
            if (debounceRef.current) clearTimeout(debounceRef.current);
        };
        // тянем данные при смене любого фильтра:
    }, [selectedEvent?.id, selectedDay, showOnlyMy, showMentored]);

    return (
        <Fade in={mounted} timeout={220}>
            <Container maxWidth="xl" sx={{ mt: 2 }}>
                <Box sx={{ mb: 2 }}>
                    <Typography variant="h5" sx={{ fontWeight: 700, mb: 0.5 }}>
                        Просмотр оценок по событию
                    </Typography>
                    <Typography variant="body2" sx={{ opacity: 0.8 }}>
                        Выберите год и этап отчётности, при необходимости отфильтруйте по дню защиты.
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
                                    top: APPBAR_H + 16,
                                }}
                            >
                                <FormControl fullWidth margin="normal">
                                    <InputLabel>Год</InputLabel>
                                    <Select value={year} onChange={handleYearChange} label="Год">
                                        {years.map(y => <MenuItem key={y} value={y}>{y}</MenuItem>)}
                                    </Select>
                                </FormControl>

                                <FormControl fullWidth margin="normal" disabled={!events.length}>
                                    <InputLabel>Этап отчетности</InputLabel>
                                    <Select
                                        value={selectedEvent || ''}
                                        onChange={handleEventChange}
                                        label="Этап отчетности">
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

                                {/* Кнопка больше не нужна — загрузка идёт реактивно */}
                                {/* Можно оставить маленький индикатор состояния */}
                                <Box sx={{ mt: 2, fontSize: 13, opacity: 0.7 }}>
                                    {selectedEvent ? (loading ? 'Загрузка…' : '') : 'Выберите событие'}
                                </Box>
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
