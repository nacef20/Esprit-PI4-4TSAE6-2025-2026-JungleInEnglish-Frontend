import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CoursesService } from '../../../services/courses.service';
import { Course, CourseLevel } from '../../../models/course.model';

@Component({
  selector: 'app-courses-management',
  standalone: false,
  templateUrl: './courses-management.component.html',
  styleUrls: ['./courses-management.component.scss']
})
export class CoursesManagementComponent implements OnInit {
  courses: Course[] = [];
  filteredCourses: Course[] = [];
  loading = true;
  error: string | null = null;
  
  // Modal
  showModal = false;
  isEditMode = false;
  saving = false;
  
  // Filters
  searchTerm = '';
  filterLevel = '';
  
  // Current course being edited/created
  currentCourse: Course = this.getEmptyCourse();

  constructor(private coursesService: CoursesService) {}

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses(): void {
    this.loading = true;
    this.error = null;
    
    this.coursesService.getAllCourses().subscribe({
      next: (data) => {
        this.courses = data;
        this.filteredCourses = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des cours';
        this.loading = false;
        console.error(err);
      }
    });
  }

  filterCourses(): void {
    this.filteredCourses = this.courses.filter(course => {
      const matchesSearch = !this.searchTerm || 
        course.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        course.description.toLowerCase().includes(this.searchTerm.toLowerCase());
      
      const matchesLevel = !this.filterLevel || course.level === this.filterLevel;
      
      return matchesSearch && matchesLevel;
    });
  }

  getCountByLevel(level: string): number {
    return this.courses.filter(c => c.level === level).length;
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.currentCourse = this.getEmptyCourse();
    this.showModal = true;
  }

  editCourse(course: Course): void {
    this.isEditMode = true;
    this.currentCourse = { ...course };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.currentCourse = this.getEmptyCourse();
  }

  saveCourse(): void {
    if (!this.currentCourse.name || !this.currentCourse.description || 
        !this.currentCourse.duration || !this.currentCourse.level) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    this.saving = true;

    const operation = this.isEditMode
      ? this.coursesService.updateCourse(this.currentCourse.id!, this.currentCourse)
      : this.coursesService.createCourse(this.currentCourse);

    operation.subscribe({
      next: () => {
        this.saving = false;
        this.closeModal();
        this.loadCourses();
        alert(this.isEditMode ? 'Cours mis à jour!' : 'Cours créé!');
      },
      error: (err) => {
        this.saving = false;
        alert('Erreur lors de l\'enregistrement');
        console.error(err);
      }
    });
  }

  deleteCourse(course: Course): void {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer "${course.name}"?`)) {
      return;
    }

    this.coursesService.deleteCourse(course.id!).subscribe({
      next: () => {
        this.loadCourses();
        alert('Cours supprimé!');
      },
      error: (err) => {
        alert('Erreur lors de la suppression');
        console.error(err);
      }
    });
  }

  getLevelClass(level: CourseLevel): string {
    switch(level) {
      case CourseLevel.BEGINNER:
        return 'level-beginner';
      case CourseLevel.INTERMEDIATE:
        return 'level-intermediate';
      case CourseLevel.ADVANCED:
        return 'level-advanced';
      default:
        return '';
    }
  }

  getLevelLabel(level: CourseLevel): string {
    switch(level) {
      case CourseLevel.BEGINNER:
        return 'Débutant';
      case CourseLevel.INTERMEDIATE:
        return 'Intermédiaire';
      case CourseLevel.ADVANCED:
        return 'Avancé';
      default:
        return level;
    }
  }

  getDefaultImage(): string {
    return 'https://images.unsplash.com/photo-1516259762381-22954d7d3ad2?w=800';
  }

  private getEmptyCourse(): Course {
    return {
      name: '',
      description: '',
      duration: '',
      level: CourseLevel.BEGINNER,
      imageUrl: ''
    };
  }
}