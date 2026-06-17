import React from 'react';
import {
    DateInput,
    NumberInput,
    SelectInput,
    SimpleForm,
    TextInput,
    useNotify,
    useRecordContext,
    useRedirect,
    Toolbar,
    SaveButton,
    DeleteButton,
} from 'react-admin';
import { Box, Divider, Typography } from '@mui/material';

const EventFormToolbar = () => {
    const record = useRecordContext();

    return (
        <Toolbar
            sx={{
                display: 'flex',
                alignItems: 'center',
                gap: 1,
                px: 0,
            }}
        >
            <SaveButton />

            <Box sx={{ flex: 1 }} />

            {record?.id && (
                <DeleteButton
                    mutationMode="pessimistic"
                    redirect="list"
                />
            )}
        </Toolbar>
    );
};

const validateForm = (values) => {
    const errors = {};

    if (!values.name) {
        errors.name = 'Обязательное поле';
    }

    if (!values.year) {
        errors.year = 'Обязательное поле';
    }

    if (!values.eventType) {
        errors.eventType = 'Обязательное поле';
    }

    if (values.maxBuildPoints === undefined || values.maxBuildPoints === null || values.maxBuildPoints === '') {
        errors.maxBuildPoints = 'Обязательное поле';
    }

    if (values.maxPresPoints === undefined || values.maxPresPoints === null || values.maxPresPoints === '') {
        errors.maxPresPoints = 'Обязательное поле';
    }

    if (values.submissionStartDate && values.submissionDeadlineDate) {
        const startDate = new Date(values.submissionStartDate);
        const deadlineDate = new Date(values.submissionDeadlineDate);

        if (startDate > deadlineDate) {
            errors.submissionDeadlineDate = 'Дата окончания должна быть не раньше даты начала';
        }
    }

    return errors;
};

const EventAdminForm = (props) => {
    const { requestMethod } = props;
    const notify = useNotify();
    const redirect = useRedirect();
    const record = useRecordContext();

    const handleKeyDown = (e) => {
        if (e.key === '-' || e.key === 'e' || e.key === 'E') {
            e.preventDefault();
        }
    };

    const handlePaste = (e) => {
        const paste = e.clipboardData.getData('text');

        if (/[-eE]/.test(paste)) {
            e.preventDefault();
        }
    };

    const handleChange = (e) => {
        const orig = e.target.value;
        const cleaned = orig.replace(/[eE-]/g, '');

        if (orig !== cleaned) {
            e.target.value = cleaned;
        } else if (cleaned.length > 2) {
            e.target.value = cleaned.slice(0, 2);
        }
    };

    const year = new Date().getFullYear();

    const handleSubmit = async (data) => {
        const { id, ...dto } = data;
        const params = record?.id ? { id: record.id, data: dto } : { data: dto };

        requestMethod('events', params)
            .then(() => {
                notify('Сохранено успешно');
                redirect('list', 'events');
            })
            .catch((error) => {
                if (error.status === 409) {
                    notify('Событие с такими параметрами уже существует', { type: 'warning' });
                } else if (error.status === 400) {
                    notify(error.body?.error || error.message || 'Некорректные данные', { type: 'warning' });
                } else {
                    notify('Неизвестная ошибка', { type: 'error' });
                }
            });
    };

    return (
        <SimpleForm
            onSubmit={handleSubmit}
            validate={validateForm}
            toolbar={<EventFormToolbar />}
            sx={{
                maxWidth: 900,
                width: '100%',
                '& .MuiFormControl-root': {
                    width: '100%',
                },
                '& .RaInput-root': {
                    width: '100%',
                },
            }}
        >
            <Box
                sx={{
                    display: 'grid',
                    gridTemplateColumns: {
                        xs: '1fr',
                        md: '1fr 1fr',
                    },
                    gap: 2,
                    width: '100%',
                }}
            >
                <TextInput
                    source="name"
                    label="Название *"
                    inputProps={{ maxLength: 32 }}
                />

                <NumberInput
                    source="year"
                    label="Год *"
                    min={year - 5}
                    max={year + 5}
                    defaultValue={year}
                    inputProps={{ maxLength: 4 }}
                />

                <SelectInput
                    source="eventType"
                    label="Тип события *"
                    choices={[
                        { id: 'IDEA', name: 'Защита идеи' },
                        { id: 'ZERO_VERSION', name: 'Защита нулевой версии' },
                        { id: 'PRE_RELEASE', name: 'Предзащита' },
                        { id: 'RELEASE', name: 'Итоговая защита' },
                    ]}
                />

                <Box />

                <NumberInput
                    source="maxBuildPoints"
                    label="Максимальная оценка за билд *"
                    min={0}
                    max={35}
                    inputProps={{
                        onKeyDown: handleKeyDown,
                        onPaste: handlePaste,
                        onChange: handleChange,
                        maxLength: 2,
                    }}
                />

                <NumberInput
                    source="maxPresPoints"
                    label="Максимальная оценка за презентацию *"
                    min={0}
                    max={15}
                    inputProps={{
                        onKeyDown: handleKeyDown,
                        onPaste: handlePaste,
                        onChange: handleChange,
                        maxLength: 2,
                    }}
                />
            </Box>

            <Box
                sx={{
                    width: '100%',
                    mt: 3,
                    p: 3,
                    borderRadius: 3,
                    border: '1px solid',
                    borderColor: 'divider',
                    bgcolor: 'background.default',
                }}
            >
                <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.75 }}>
                    Период сдачи материалов
                </Typography>

                <Typography variant="body2" color="text.secondary" sx={{ mb: 2.5 }}>
                    В этот период капитан проекта сможет отправлять ссылки на презентацию, репозиторий, релиз и комментарий.
                </Typography>

                <Divider sx={{ mb: 3 }} />

                <Box
                    sx={{
                        display: 'grid',
                        gridTemplateColumns: {
                            xs: '1fr',
                            md: '1fr 1fr',
                        },
                        gap: 3,
                        width: '100%',
                        alignItems: 'start',
                    }}
                >
                    <Box>
                        <DateInput
                            source="submissionStartDate"
                            label="Дата начала сдачи"
                            helperText={false}
                        />

                        <Typography
                            variant="caption"
                            color="text.secondary"
                            sx={{
                                display: 'block',
                                mt: -0.5,
                                pl: 0.5,
                                lineHeight: 1.4,
                            }}
                        >
                        </Typography>
                    </Box>

                    <Box>
                        <DateInput
                            source="submissionDeadlineDate"
                            label="Дедлайн сдачи"
                            helperText={false}
                        />

                        <Typography
                            variant="caption"
                            color="text.secondary"
                            sx={{
                                display: 'block',
                                mt: -0.5,
                                pl: 0.5,
                                lineHeight: 1.4,
                            }}
                        >
                        </Typography>
                    </Box>
                </Box>
            </Box>
        </SimpleForm>
    );
};

export default EventAdminForm;