import React, { useEffect, useMemo, useState } from 'react';
import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    Checkbox,
    Chip,
    CircularProgress,
    Container,
    Divider,
    FormControl,
    FormControlLabel,
    InputLabel,
    Link,
    MenuItem,
    Paper,
    Select,
    Stack,
    TextField,
    Typography,
} from '@mui/material';
import OpenInNewIcon from '@mui/icons-material/OpenInNew';

const statusLabels = {
    NOT_CONFIGURED: 'Период сдачи не настроен',
    FUTURE: 'Будущий этап',
    OPEN: 'Открыт для сдачи',
    CLOSED: 'Завершён',
};

const statusColors = {
    NOT_CONFIGURED: 'default',
    FUTURE: 'info',
    OPEN: 'success',
    CLOSED: 'default',
};

const formatDate = (value) => {
    if (!value) {
        return 'не указана';
    }

    return new Date(value).toLocaleDateString('ru-RU');
};

const formatDateTime = (value) => {
    if (!value) {
        return 'не было загрузки';
    }

    return new Date(value).toLocaleString('ru-RU');
};

const getResponseItems = (data) => data.content || data.data || [];

const ExternalLink = ({ href, children }) => {
    if (!href) {
        return (
            <Typography variant="body2" color="text.secondary">
                не указана
            </Typography>
        );
    }

    return (
        <Link
            href={href}
            target="_blank"
            rel="noopener noreferrer"
            sx={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: 0.5,
                wordBreak: 'break-all',
            }}
        >
            {children}
            <OpenInNewIcon sx={{ fontSize: 16 }} />
        </Link>
    );
};

const ProjectSubmissionsReviewPage = () => {
    const [events, setEvents] = useState([]);
    const [selectedEventId, setSelectedEventId] = useState('');
    const [submissions, setSubmissions] = useState([]);
    const [eventsLoading, setEventsLoading] = useState(true);
    const [submissionsLoading, setSubmissionsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [search, setSearch] = useState('');
    const [submittedOnly, setSubmittedOnly] = useState(false);

    const currentYear = new Date().getFullYear();

    const selectedEvent = useMemo(
        () => events.find((event) => event.id === selectedEventId) || null,
        [events, selectedEventId]
    );

    const filteredSubmissions = useMemo(() => {
        const normalizedSearch = search.trim().toLowerCase();

        return submissions.filter((item) => {
            if (submittedOnly && !item.submitted) {
                return false;
            }

            if (!normalizedSearch) {
                return true;
            }

            const projectName = item.projectName?.toLowerCase() || '';
            const captainName = item.captainFullName?.toLowerCase() || '';
            const captainLogin = item.captainLogin?.toLowerCase() || '';

            return projectName.includes(normalizedSearch)
                || captainName.includes(normalizedSearch)
                || captainLogin.includes(normalizedSearch);
        });
    }, [submissions, search, submittedOnly]);

    const submittedCount = useMemo(
        () => submissions.filter((item) => item.submitted).length,
        [submissions]
    );

    const loadEvents = async () => {
        setEventsLoading(true);
        setError(null);

        try {
            const response = await fetch(`/api/v1/jury/submissions/events?year=${currentYear}`, {
                credentials: 'include',
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || 'Не удалось загрузить этапы отчётности');
            }

            const loadedEvents = data.events || [];
            const defaultEventId = data.defaultEventId || loadedEvents[0]?.id || '';

            setEvents(loadedEvents);
            setSelectedEventId(defaultEventId);
        } catch (requestError) {
            setError(requestError.message || 'Ошибка загрузки этапов отчётности');
        } finally {
            setEventsLoading(false);
        }
    };

    const loadSubmissions = async (eventId) => {
        if (!eventId) {
            setSubmissions([]);
            return;
        }

        setSubmissionsLoading(true);
        setError(null);

        try {
            const response = await fetch(`/api/v1/jury/submissions/events/${eventId}?limit=200&offset=0`, {
                credentials: 'include',
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || 'Не удалось загрузить материалы проектов');
            }

            setSubmissions(getResponseItems(data));
        } catch (requestError) {
            setError(requestError.message || 'Ошибка загрузки материалов проектов');
        } finally {
            setSubmissionsLoading(false);
        }
    };

    useEffect(() => {
        loadEvents();
    }, []);

    useEffect(() => {
        if (selectedEventId) {
            loadSubmissions(selectedEventId);
        }
    }, [selectedEventId]);

    const handleEventChange = (event) => {
        setSelectedEventId(event.target.value);
        setSearch('');
        setSubmittedOnly(false);
    };

    if (eventsLoading) {
        return (
            <Box sx={{ pt: 10, pb: 6, minHeight: '100vh' }}>
                <Container maxWidth="lg">
                    <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
                        <CircularProgress />
                    </Box>
                </Container>
            </Box>
        );
    }

    return (
        <Box sx={{ pt: 10, pb: 6, minHeight: '100vh' }}>
            <Container maxWidth="lg">
                <Box sx={{ mb: 4 }}>
                    <Typography variant="h3" sx={{ fontWeight: 800, mb: 1 }}>
                        Загрузки проектов
                    </Typography>
                </Box>

                {error && (
                    <Alert severity="error" sx={{ mb: 3 }}>
                        {error}
                    </Alert>
                )}

                <Paper sx={{ p: { xs: 2, sm: 3 }, borderRadius: 3, mb: 3 }}>
                    {events.length === 0 ? (
                        <Alert severity="info">
                            Для {currentYear} года пока нет этапов отчётности.
                        </Alert>
                    ) : (
                        <Stack spacing={3}>
                            <Box
                                sx={{
                                    display: 'grid',
                                    gridTemplateColumns: {
                                        xs: '1fr',
                                        md: '1fr 1fr',
                                    },
                                    gap: 2,
                                    alignItems: 'start',
                                }}
                            >
                                <FormControl fullWidth>
                                    <InputLabel id="submission-event-select-label">
                                        Этап отчётности
                                    </InputLabel>

                                    <Select
                                        labelId="submission-event-select-label"
                                        value={selectedEventId}
                                        label="Этап отчётности"
                                        onChange={handleEventChange}
                                    >
                                        {events.map((event) => (
                                            <MenuItem key={event.id} value={event.id}>
                                                {event.name} — {statusLabels[event.submissionStatus] || event.submissionStatus}
                                            </MenuItem>
                                        ))}
                                    </Select>
                                </FormControl>

                                <TextField
                                    label="Поиск по проекту или капитану"
                                    value={search}
                                    onChange={(event) => setSearch(event.target.value)}
                                    fullWidth
                                />
                            </Box>

                            <FormControlLabel
                                control={
                                    <Checkbox
                                        checked={submittedOnly}
                                        onChange={(event) => setSubmittedOnly(event.target.checked)}
                                    />
                                }
                                label="Отобразить только загруженные"
                            />

                            {selectedEvent && (
                                <Box>
                                    <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap" sx={{ mb: 1.5 }}>
                                        <Chip
                                            label={statusLabels[selectedEvent.submissionStatus] || selectedEvent.submissionStatus}
                                            color={statusColors[selectedEvent.submissionStatus] || 'default'}
                                        />

                                        <Chip label={`${selectedEvent.year} год`} variant="outlined" />

                                        {selectedEvent.eventType && (
                                            <Chip label={selectedEvent.eventType} variant="outlined" />
                                        )}
                                    </Stack>

                                    <Stack spacing={0.75}>
                                        <Typography variant="body2">
                                            <Box component="span" sx={{ fontWeight: 700 }}>
                                                Начало сдачи:
                                            </Box>{' '}
                                            {formatDate(selectedEvent.submissionStartDate)}
                                        </Typography>

                                        <Typography variant="body2">
                                            <Box component="span" sx={{ fontWeight: 700 }}>
                                                Дедлайн:
                                            </Box>{' '}
                                            {formatDate(selectedEvent.submissionDeadlineDate)}
                                        </Typography>

                                        <Typography variant="body2">
                                            <Box component="span" sx={{ fontWeight: 700 }}>
                                                Загружено:
                                            </Box>{' '}
                                            {submittedCount} из {submissions.length}
                                        </Typography>

                                        <Typography variant="body2">
                                            <Box component="span" sx={{ fontWeight: 700 }}>
                                                Отображается:
                                            </Box>{' '}
                                            {filteredSubmissions.length}
                                        </Typography>
                                    </Stack>
                                </Box>
                            )}
                        </Stack>
                    )}
                </Paper>

                {submissionsLoading && (
                    <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
                        <CircularProgress />
                    </Box>
                )}

                {!submissionsLoading && selectedEventId && submissions.length === 0 && (
                    <Alert severity="info">
                        Для выбранного этапа пока нет привязанных проектов.
                    </Alert>
                )}

                {!submissionsLoading && submissions.length > 0 && filteredSubmissions.length === 0 && (
                    <Alert severity="info">
                        По текущим фильтрам ничего не найдено.
                    </Alert>
                )}

                {!submissionsLoading && filteredSubmissions.length > 0 && (
                    <Stack spacing={2.5}>
                        {filteredSubmissions.map((item) => (
                            <Card key={item.projectId} sx={{ borderRadius: 3 }}>
                                <CardContent sx={{ p: { xs: 2.5, sm: 3 } }}>
                                    <Box
                                        sx={{
                                            display: 'flex',
                                            justifyContent: 'space-between',
                                            alignItems: 'flex-start',
                                            gap: 2,
                                            flexWrap: 'wrap',
                                            mb: 2,
                                        }}
                                    >
                                        <Box>
                                            <Typography variant="h5" sx={{ fontWeight: 750, mb: 0.75 }}>
                                                {item.projectName}
                                            </Typography>

                                            <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
                                                {item.projectExternalId && (
                                                    <Chip size="small" label={`ID ${item.projectExternalId}`} variant="outlined" />
                                                )}

                                                {item.projectTrack && (
                                                    <Chip size="small" label={item.projectTrack} variant="outlined" />
                                                )}
                                            </Stack>
                                        </Box>

                                        {item.submitted ? (
                                            <Chip label="Сдано" color="success" />
                                        ) : (
                                            <Chip label="Не сдано" color="warning" variant="outlined" />
                                        )}
                                    </Box>

                                    <Stack spacing={0.75} sx={{ mb: 2 }}>
                                        <Typography variant="body2">
                                            <Box component="span" sx={{ fontWeight: 700 }}>
                                                Капитан:
                                            </Box>{' '}
                                            {item.captainFullName || 'не назначен'}
                                            {item.captainLogin ? ` (${item.captainLogin})` : ''}
                                        </Typography>

                                        <Typography variant="body2">
                                            <Box component="span" sx={{ fontWeight: 700 }}>
                                                Технологии:
                                            </Box>{' '}
                                            {item.projectTechnologies || 'не указаны'}
                                        </Typography>

                                        <Typography variant="body2">
                                            <Box component="span" sx={{ fontWeight: 700 }}>
                                                Последнее обновление:
                                            </Box>{' '}
                                            {formatDateTime(item.updatedAt)}
                                        </Typography>
                                    </Stack>

                                    <Divider sx={{ my: 2 }} />

                                    {item.submitted ? (
                                        <Box
                                            sx={{
                                                display: 'grid',
                                                gridTemplateColumns: {
                                                    xs: '1fr',
                                                    md: '1fr 1fr',
                                                },
                                                gap: 2,
                                            }}
                                        >
                                            <Box>
                                                <Typography variant="subtitle2" sx={{ mb: 0.5 }}>
                                                    Презентация
                                                </Typography>
                                                <ExternalLink href={item.presentationUrl}>
                                                    Открыть презентацию
                                                </ExternalLink>
                                            </Box>

                                            <Box>
                                                <Typography variant="subtitle2" sx={{ mb: 0.5 }}>
                                                    Репозиторий
                                                </Typography>
                                                <ExternalLink href={item.repositoryUrl}>
                                                    Открыть репозиторий
                                                </ExternalLink>
                                            </Box>

                                            <Box>
                                                <Typography variant="subtitle2" sx={{ mb: 0.5 }}>
                                                    Релиз
                                                </Typography>
                                                <ExternalLink href={item.releaseUrl}>
                                                    Открыть релиз
                                                </ExternalLink>
                                            </Box>

                                            <Box>
                                                <Typography variant="subtitle2" sx={{ mb: 0.5 }}>
                                                    Комментарий
                                                </Typography>
                                                <Typography
                                                    variant="body2"
                                                    color={item.comment ? 'text.primary' : 'text.secondary'}
                                                    sx={{ whiteSpace: 'pre-line' }}
                                                >
                                                    {item.comment || 'не указан'}
                                                </Typography>
                                            </Box>
                                        </Box>
                                    ) : (
                                        <Alert severity="info">
                                            Капитан команды ещё не загрузил материалы для выбранного этапа.
                                        </Alert>
                                    )}
                                </CardContent>
                            </Card>
                        ))}
                    </Stack>
                )}
            </Container>
        </Box>
    );
};

export default ProjectSubmissionsReviewPage;