import React from 'react';
import {Create, Edit, useDataProvider} from 'react-admin';
import EventAdminForm from "./EventAdminForm";


export const EventCreate = (props) => {
    const dataProvider = useDataProvider();

    return (
        <Create title="Добавить событие" {...props} redirect="list" >
            <EventAdminForm requestMethod={dataProvider.create}/>
        </Create>
    );
};

export const EventEdit = (props) => {
    const dataProvider = useDataProvider();

    return (
        <Edit title="Изменить событие" {...props} redirect="list">
            <EventAdminForm requestMethod={dataProvider.update}/>
        </Edit>
    );
};


