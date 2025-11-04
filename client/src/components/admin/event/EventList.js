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
        <List {...props} exporter={false} perPage={5}>
            <Datagrid>
                <TextField source="name" label="Название" />
                <FunctionField
                    source="eventType"
                    label="Тип события"
                    render={renderEventType}
                />
                <TextField source="year" label="Год" />
                <TextField source="maxBuildPoints" label="Максимум за билд" />
                <TextField source="maxPresPoints" label="Максимум за презентацию" />
            </Datagrid>
        </List>
    );
};

export default EventList;