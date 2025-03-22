import React from 'react';
import {Datagrid, FunctionField, List, TextField} from 'react-admin';

const ProjectList = (props) => {

    const renderProjectType = (record) => {
        switch (String(record.type)) {
            case 'WEB_APP':
                return 'Веб-приложение';
            case 'DESKTOP_APP':
                return 'Десктопное приложение';
            case 'MOBILE_APP':
                return 'Предзащита';
            case 'GAME':
                return 'Игра';
            case 'MOD':
                return 'Мод';
            case 'TELEGRAM_BOT':
                return 'Телеграм-бот';
            default:
                return record.projectType;
        }
    };


    return (
        <List {...props}>
            <Datagrid>
                <TextField source="name" label="Название" />
                <TextField source="description" label="Описание" />
                <FunctionField
                    source="type"
                    label="Тип Проекта"
                    render={renderProjectType}
                />
                <TextField source="year" label="Год" />
            </Datagrid>
        </List>
    );
}

export default ProjectList;
