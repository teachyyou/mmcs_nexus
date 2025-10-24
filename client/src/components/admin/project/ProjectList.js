import React from 'react';
import { Datagrid, FunctionField, List, TextField, NumberField } from 'react-admin';

const renderProjectType = (record) => {
    switch (String(record.type)) {
        case 'WEB_APP':
            return 'Веб-приложение';
        case 'DESKTOP_APP':
            return 'Десктопное приложение';
        case 'MOBILE_APP':
            return 'Мобильное приложение';
        case 'GAME':
            return 'Игра';
        case 'GAME_MOD':
            return 'Мод';
        case 'TELEGRAM_BOT':
            return 'Телеграм-бот';
        default:
            return record?.type ?? '-';
    }
};

const ProjectList = (props) => (
    <List {...props} exporter={false}>
        <Datagrid rowClick="edit">
            <NumberField source="externalId" label="externalid" />
            <TextField source="name" label="Название" />
            <FunctionField
                source="type"
                label="Тип"
                render={renderProjectType}
            />
            <NumberField source="quantityOfStudents" label="Число участников" />
            <TextField source="track" label="Трек" />
        </Datagrid>
    </List>
);

export default ProjectList;
