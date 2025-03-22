import React from 'react';
import { List, Datagrid, TextField, FunctionField } from 'react-admin';

const EventList = (props) => {
    const renderEventType = (record) => {
        switch (String(record.eventType)) {
            case 'IDEA':
                return 'Защита идеи';
            case 'ZERO_VERSION':
                return 'Защита нулевой версии';
            case 'PRE_RELEASE':
                return 'Предзащита';
            case 'RELEASE':
                return 'Итоговая защита';
            default:
                return record.eventType;
        }
    };

    return (
        <List {...props}>
            <Datagrid>
                <TextField source="name" label="Название" />
                <FunctionField
                    source="eventType"
                    label="Тип события"
                    render={renderEventType}
                />
                <TextField source="year" label="Год" />
            </Datagrid>
        </List>
    );
};

export default EventList;