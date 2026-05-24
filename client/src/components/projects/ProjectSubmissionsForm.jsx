import React, { useEffect, useMemo, useState } from 'react';
import {
    Alert,
    Box,
    Button,
    CircularProgress,
    Divider,
    FormControl,
    InputLabel,
    MenuItem,
    Paper,
    Select,
    Stack,
    TextField,
    Typography,
} from '@mui/material';

const emptyForm = {
    presentationUrl: '',
    repositoryUrl: '',
    releaseUrl: '',
    comment: '',
};

const statusLabels = {
    NOT_CONFIGURED: 'Период сдачи не настроен',
    FUTURE: 'Загрузка будет доступна позже',
    OPEN: 'Можно редактировать',
    CLOSED: 'Этап завершён',
};

const formatDate = (value) => {
    if (!value) {
        return 'не указана';
    }

    return new Date(value).toLocaleDateString('ru-RU');
};

const normalizeSubmission = (submission) => ({
    presentationUrl: submission?.presentationUrl || '',
    repositoryUrl: submission?.repositoryUrl || '',
    releaseUrl: submission?.releaseUrl || '',
    comment: submission?.comment || '',
});

const ProjectSubmissionsForm = ({ projectId }) => {
    const [items, setItems] = useState([]);
    const [selectedEventId, setSelectedEventId] = useState('');
    const [form, setForm] = useState(emptyForm);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);
    const [successMessage, setSuccessMessage] = useState(null);

    const selectedItem = useMemo(
        () => items.find((item) => item.eventId === selectedEventId) || null,
        [items, selectedEventId]
    );

    const loadSubmissions = async () => {
        setLoading(true);
        setError(null);
        setSuccessMessage(null);

        try {
            const response = await fetch(`/api/v1/user/projects/${projectId}/submissions`, {
                credentials: 'include',
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || 'Не удалось загрузить форму сдачи материалов');
            }

            const loadedItems = data.items || [];
            const defaultEventId = data.defaultEventId || loadedItems[0]?.eventId || '';

            setItems(loadedItems);
            setSelectedEventId(defaultEventId);

            const defaultItem = loadedItems.find((item) => item.eventId === defaultEventId);

            setForm(normalizeSubmission(defaultItem?.submission));
        } catch (requestError) {
            setError(requestError.message || 'Ошибка загрузки формы сдачи материалов');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadSubmissions();
    }, [projectId]);

    const handleEventChange = (event) => {
        const nextEventId = event.target.value;
        const nextItem = items.find((item) => item.eventId === nextEventId);

        setSelectedEventId(nextEventId);
        setForm(normalizeSubmission(nextItem?.submission));
        setError(null);
        setSuccessMessage(null);
    };

    const handleFieldChange = (field) => (event) => {
        setForm((current) => ({
            ...current,
            [field]: event.target.value,
        }));
    };

    const saveSubmission = async () => {
        if (!selectedItem) {
            return;
        }

        setSaving(true);
        setError(null);
        setSuccessMessage(null);

        try {
            const response = await fetch(
                `/api/v1/user/projects/${projectId}/events/${selectedItem.eventId}/submission`,
                {
                    method: 'PUT',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(form),
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || 'Не удалось сохранить сдачу материалов');
            }

            setItems((current) => current.map((item) => (
                item.eventId === selectedItem.eventId
                    ? { ...item, submission: data }
                    : item
            )));

            setForm(normalizeSubmission(data));
            setSuccessMessage('Материалы сохранены');
        } catch (requestError) {
            setError(requestError.message || 'Ошибка сохранения материалов');
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <Paper sx={{ mt: 4, p: 3, borderRadius: 3 }}>
                <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                    <CircularProgress />
                </Box>
            </Paper>
        );
    }

    if (error && items.length === 0) {
        return (
            <Paper sx={{ mt: 4, p: 3, borderRadius: 3 }}>
                <Alert severity="error">{error}</Alert>
            </Paper>
        );
    }

    if (items.length === 0) {
        return (
            <Paper sx={{ mt: 4, p: 3, borderRadius: 3 }}>
                <Typography variant="h5" sx={{ fontWeight: 750, mb: 1 }}>
                    Сдача материалов
                </Typography>

                <Alert severity="info">
                    Для этого проекта пока не назначены этапы отчётности.
                </Alert>
            </Paper>
        );
    }

    const editable = Boolean(selectedItem?.editable);
    const isFuture = selectedItem?.submissionStatus === 'FUTURE';
    const isClosed = selectedItem?.submissionStatus === 'CLOSED';
    const isNotConfigured = selectedItem?.submissionStatus === 'NOT_CONFIGURED';

    return (
        <Paper sx={{ mt: 4, p: { xs: 3, sm: 4 }, borderRadius: 3 }}>
            <Typography variant="h5" sx={{ fontWeight: 750, mb: 1 }}>
                Сдача материалов
            </Typography>

            <Typography color="text.secondary" sx={{ mb: 3 }}>
                Выберите этап отчётности и отправьте ссылки на материалы проекта.
            </Typography>

            <FormControl fullWidth sx={{ mb: 3 }}>
                <InputLabel id="submission-event-select-label">Этап отчётности</InputLabel>
                <Select
                    labelId="submission-event-select-label"
                    value={selectedEventId}
                    label="Этап отчётности"
                    onChange={handleEventChange}
                >
                    {items.map((item) => (
                        <MenuItem key={item.eventId} value={item.eventId}>
                            {item.eventName} — {statusLabels[item.submissionStatus] || item.submissionStatus}
                        </MenuItem>
                    ))}
                </Select>
            </FormControl>

            {selectedItem && (
                <>
                    <Stack spacing={0.75} sx={{ mb: 3 }}>
                        <Typography variant="body2">
                            <Box component="span" sx={{ fontWeight: 700 }}>
                                Начало сдачи:
                            </Box>{' '}
                            {formatDate(selectedItem.submissionStartDate)}
                        </Typography>

                        <Typography variant="body2">
                            <Box component="span" sx={{ fontWeight: 700 }}>
                                Дедлайн:
                            </Box>{' '}
                            {formatDate(selectedItem.submissionDeadlineDate)}
                        </Typography>
                    </Stack>

                    {error && (
                        <Alert severity="error" sx={{ mb: 3 }}>
                            {error}
                        </Alert>
                    )}

                    {successMessage && (
                        <Alert severity="success" sx={{ mb: 3 }}>
                            {successMessage}
                        </Alert>
                    )}

                    <Divider sx={{ mb: 3 }} />

                    {(isFuture || isNotConfigured) && !selectedItem.submission && (
                        <Alert severity="info">
                            {selectedItem.message || 'Загрузка будет доступна позже'}
                        </Alert>
                    )}

                    {(!isFuture && !isNotConfigured) || selectedItem.submission ? (
                        <Stack spacing={2}>
                            <TextField
                                label="Ссылка на презентацию"
                                value={form.presentationUrl}
                                onChange={handleFieldChange('presentationUrl')}
                                disabled={!editable || saving}
                                fullWidth
                            />

                            <TextField
                                label="Ссылка на репозиторий"
                                value={form.repositoryUrl}
                                onChange={handleFieldChange('repositoryUrl')}
                                disabled={!editable || saving}
                                fullWidth
                            />

                            <TextField
                                label="Ссылка на релиз"
                                value={form.releaseUrl}
                                onChange={handleFieldChange('releaseUrl')}
                                disabled={!editable || saving}
                                fullWidth
                            />

                            <TextField
                                label="Комментарий"
                                value={form.comment}
                                onChange={handleFieldChange('comment')}
                                disabled={!editable || saving}
                                fullWidth
                                multiline
                                minRows={4}
                            />

                            {isClosed && (
                                <Alert severity="info">
                                    Этап завершён, редактирование недоступно. Материалы доступны только для просмотра.
                                </Alert>
                            )}

                            {editable && (
                                <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
                                    <Button
                                        variant="contained"
                                        disabled={saving}
                                        onClick={saveSubmission}
                                    >
                                        {saving ? 'Сохраняем…' : 'Сохранить материалы'}
                                    </Button>
                                </Box>
                            )}
                        </Stack>
                    ) : null}
                </>
            )}
        </Paper>
    );
};

export default ProjectSubmissionsForm;