import React, { useState } from 'react';
import {
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    Box
} from '@mui/material';
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

    // Формируем карту оценок для быстрого доступа по projectId и juryId
    const gradeMap = {};
    if (grades && grades.rows) {
        grades.rows.forEach((row) => {
            gradeMap[row.projectId] = {};
            row.tableRow.forEach((item) => {
                gradeMap[row.projectId][item.juryId] = item;
            });
        });
    }

    return (
        <>
            <TableContainer component={Paper} sx={{ mt: 2 }}>
                <Table sx={{ minWidth: 800 }}>
                    <TableHead>
                        <TableRow>
                            <TableCell sx={{ fontWeight: 'bold', border: '2px solid black' }}>
                                Проекты / Жюри
                            </TableCell>
                            {grades.juries.map((jury) => (
                                <TableCell
                                    key={jury.id}
                                    align="center"
                                    sx={{ fontWeight: 'bold', border: '2px solid black' }}
                                >
                                    {jury.firstName} {jury.lastName}
                                </TableCell>
                            ))}
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {grades.projects.map((project) => (
                            <TableRow key={project.id}>
                                <TableCell sx={{ fontWeight: 'bold', border: '2px solid black' }}>
                                    {project.name}
                                </TableCell>
                                {grades.juries.map((jury) => {
                                    const gradeItem = gradeMap[project.id]?.[jury.id];
                                    return (
                                        <TableCell
                                            key={`${project.id}-${jury.id}`}
                                            align="center"
                                            onClick={() => handleCellClick(gradeItem, project.id, jury.id)}
                                            sx={{
                                                cursor: 'pointer',
                                                border: '2px solid black',
                                                p: 1
                                            }}
                                        >
                                            {gradeItem ? (
                                                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                                                    {/* Две миниячейки для оценки за билд и презентацию */}
                                                    <Box sx={{ display: 'flex', gap: 1 }}>
                                                        <Box sx={{
                                                            border: '1px solid black',
                                                            flex: 1,
                                                            textAlign: 'center',
                                                            p: 0.5
                                                        }}>
                                                            {gradeItem.buildPoints !== undefined ? gradeItem.buildPoints : '-'}
                                                        </Box>
                                                        <Box sx={{
                                                            border: '1px solid black',
                                                            flex: 1,
                                                            textAlign: 'center',
                                                            p: 0.5
                                                        }}>
                                                            {gradeItem.presPoints !== undefined ? gradeItem.presPoints : '-'}
                                                        </Box>
                                                    </Box>
                                                    {/* Комментарий с усечением, если слишком длинный */}
                                                    <Box sx={{ fontSize: '0.8rem' }}>
                                                        {gradeItem.comment && gradeItem.comment.length > 20
                                                            ? `${gradeItem.comment.substring(0, 20)}...`
                                                            : gradeItem.comment || ''}
                                                    </Box>
                                                </Box>
                                            ) : (
                                                'Не оценено'
                                            )}
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
                grades={grades}
            />
        </>
    );
};

export default GradeTable;
