import {
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
                validate={required()}
            />
            <TextInput
                source="year"
                name="year"
                label="Тип события"
                validate={required()}
            />
            <SelectInput
                source="eventType"
                name="eventType"
                choices={[
                    { id: 'IDEA', name: 'Защита идеи' },
                    { id: 'ZERO_VERSION', name: 'Защита нулевой версии' },
                    { id: 'PRE_RELEASE', name: 'Предзащита' },
                    { id: 'RELEASE', name: 'Итоговая защита' },
                ]}
                validate={required()}
            />
        </SimpleForm>
    );
};

export default EventAdminForm;