import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

const CompleteProfilePage = () => {
    const [firstName, setFirstName] = useState("");
    const [lastName, setLastName] = useState("");
    const [course, setCourse] = useState("");
    const [userGroup, setUserGroup] = useState("");
    const navigate = useNavigate();
    const location = useLocation();
    const [githubLogin, setGithubLogin] = useState("");

    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const githubLoginParam = params.get("githubLogin");
        setGithubLogin(githubLoginParam);
    }, [location]);

    const handleSubmit = async (event) => {
        event.preventDefault();
        const response = await fetch('http://localhost:8080/api/v1/auth/complete-profile?githubLogin=' + githubLogin, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify({
                firstName,
                lastName,
                course,
                userGroup: parseInt(userGroup),
            }),
        });

        if (response.ok) {
            navigate('/');
        } else {
            console.error('Profile completion failed.');
        }
    };

    return (
        <div>
            <h1>Complete your profile</h1>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>First Name</label>
                    <input type="text" value={firstName} onChange={(e) => setFirstName(e.target.value)} required />
                </div>
                <div>
                    <label>Last Name</label>
                    <input type="text" value={lastName} onChange={(e) => setLastName(e.target.value)} required />
                </div>
                <div>
                    <label>Course</label>
                    <input type="text" value={course} onChange={(e) => setCourse(e.target.value)} required />
                </div>
                <div>
                    <label>Group</label>
                    <input type="number" value={userGroup} onChange={(e) => setUserGroup(e.target.value)} required />
                </div>
                <button type="submit">Submit</button>
            </form>
        </div>
    );
};

export default CompleteProfilePage;
