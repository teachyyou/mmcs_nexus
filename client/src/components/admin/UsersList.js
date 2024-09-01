// src/UsersList.js

import React, { useEffect, useState } from 'react';

const UsersList = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch('http://localhost:8080/api/v1/admin/users/list', {
            credentials: 'include',
            headers: { 'Content-type': 'application/json' },
        })
            .then(
                response =>response.json())
            .then(data => {
                setUsers(data);
                setLoading(false);
            })
    }, []);

    if (!loading) {
        return (
            <div>
                <h1>Users List</h1>
                <table border="1" cellPadding="10" cellSpacing="0">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>First Name</th>
                        <th>Last Name</th>
                        <th>Login</th>
                        <th>User Group</th>
                        <th>Status</th>
                        <th>Role</th>
                    </tr>
                    </thead>
                    <tbody>
                    {users.map(user => (
                        <tr key={user.id}>
                            <td>{user.id}</td>
                            <td>{user.firstName}</td>
                            <td>{user.lastName}</td>
                            <td>{user.login}</td>
                            <td>{user.userGroup}</td>
                            <td>{user.status}</td>
                            <td>{user.role}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        );
    }


};

export default UsersList;
