import {
    NumberInput,
    required,
    SelectInput,
    SimpleForm,
    TextInput,
    useNotify,
    useRecordContext,
    useRedirect
} from "react-admin";
import AdminToolbar from "../AdminToolbar";
import React from "react";

const EventAdminForm = (props) => {

    const { requestMethod } = props;
    const notify = useNotify();
    const redirect = useRedirect();
    const record = useRecordContext();


    const handleKeyDown = (event) => {
        if (event.key === '-' || event.key === 'e' || event.key === 'E') {
            event.preventDefault();
        }
    };

    const handlePaste = (event) => {
        const pasteData = event.clipboardData.getData('text');
        if (/[-eE]/.test(pasteData)) {
            event.preventDefault();
        }
    };

    const handleChange = (event) => {
        const originalValue = event.target.value;
        const cleanedValue = originalValue.replace(/[eE-]/g, '');
        if (originalValue !== cleanedValue) {
            event.target.value = cleanedValue;
        }
        else if (cleanedValue.length > 2) {
            event.target.value = cleanedValue.slice(0, 2);
        }
    };

    const year = new Date().getFullYear();

    const requiredWithMessage = required("Обязательное поле");

    const handleSubmit =  async (data) => {

        const { id, ...dataWithoutId } = data;
        const params = record?.id ? { id: record.id, data: dataWithoutId } : { data: dataWithoutId };

        requestMethod('events', params)
            .then(() => {
                notify('Сохранено успешно');
                redirect('list', 'events');
            })
            .catch((error) => {
                if (error.status === 409) {
                    notify('Событие с такими параметрами уже существует', { type: 'warning' });
                } else {
                    notify('Неизвестная ошибка', { type: 'error' });
                }
            });
    };

    return (
        <SimpleForm onSubmit={handleSubmit} toolbar={<AdminToolbar />}>
            <TextInput
                source="name"
                name="name"
                label="Название"
                validate={requiredWithMessage}
                inputProps={{ maxLength: 32 }}
            />
            <NumberInput
                source="year"
                name="year"
                label="Год"
                min={year-5}
                max={year+5}
                defaultValue={year}
                validate={requiredWithMessage}
                inputProps={{ maxLength: 4 }}
            />
            <SelectInput
                source="eventType"
                name="eventType"
                label="Тип события"
                choices={[
                    { id: 'IDEA', name: 'Защита идеи' },
                    { id: 'ZERO_VERSION', name: 'Защита нулевой версии' },
                    { id: 'PRE_RELEASE', name: 'Предзащита' },
                    { id: 'RELEASE', name: 'Итоговая защита' },
                ]}
                validate={requiredWithMessage}
            />
            <NumberInput
                source="maxBuildPoints"
                name="maxBuildPoints"
                label="Максимальная оценка за билд"
                validate={requiredWithMessage}
                inputProps={{
                    onKeyDown: handleKeyDown,
                    onPaste: handlePaste,
                    onChange: handleChange,
                    maxLength: 2
                }}
            />
            <NumberInput
                source="maxPresPoints"
                name="maxPresPoints"
                label="Максимальная оценка за презентацию"
                validate={requiredWithMessage}
                inputProps={{
                    onKeyDown: handleKeyDown,
                    onPaste: handlePaste,
                    onChange: handleChange,
                    maxLength: 2
                }}
            />
        </SimpleForm>
    );
};

export default EventAdminForm;