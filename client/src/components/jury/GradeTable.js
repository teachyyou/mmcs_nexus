import React, { useState } from 'react';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper } from '@mui/material';
import GradeEditorModal from './GradeEditorModal';

const GradeTable = ({ grades }) => {
    const [selectedGrade, setSelectedGrade] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedJuryId, setSelectedJuryId] = useState(null);
    const [selectedProjectId, setSelectedProjectId] = useState(null);

    const handleCellClick = (grade, projectId, juryId) => {
        setSelectedGrade(grade);
        setSelectedJuryId(juryId);
        setSelectedProjectId(projectId);
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
    };

    return (
        <>
            <TableContainer component={Paper} sx={{ marginTop: 2 }}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Проекты / Жюри</TableCell>
                            {grades.juries.map((jury) => (
                                <TableCell key={jury.id}>{jury.firstName} {jury.lastName}</TableCell>
                            ))}
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {grades.projects.map((project) => (
                            <TableRow key={project.id}>
                                <TableCell>{project.name}</TableCell>
                                {grades.juries.map((jury) => {
                                    const gradeRow = grades.rows.find(row => row.projectId === project.id);
                                    const grade = gradeRow ? gradeRow.tableRow.find(item => item.id.juryId === jury.id) : null;
                                    return (
                                        <TableCell
                                            key={jury.id}
                                            onClick={() => handleCellClick(grade, project.id, jury.id)}
                                            sx={{
                                                cursor: 'pointer',
                                                border: '1px solid rgba(0, 0, 0, 0.1)',
                                                '&:hover': {
                                                    backgroundColor: 'rgba(33, 150, 243, 0.2)',
                                                },
                                            }}
                                        >
                                            {grade ? grade.comment : 'Не оценено'}
                                        </TableCell>
                                    );
                                })}
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>

            <GradeEditorModal
                open={isModalOpen}
                onClose={handleCloseModal}
                grade={selectedGrade}
                projectId={selectedProjectId}
                juryId={selectedJuryId}
                grades={grades} // Передаем все данные
            />
        </>
    );
};

export default GradeTable;
