import { Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class ValidationService {
    /**
     * Maps backend validation errors (from MethodArgumentNotValidException) to Angular form controls.
     * Expects response body like: { fieldErrors: { "fieldName": "Error Message" } }
     */
    handleBackendErrors(form: FormGroup, error: any): void {
        if (error instanceof HttpErrorResponse && error.status === 400 && error.error?.fieldErrors) {
            const fieldErrors = error.error.fieldErrors;
            Object.keys(fieldErrors).forEach(field => {
                const control = form.get(field);
                if (control) {
                    control.setErrors({ backend: fieldErrors[field] });
                }
            });
        }
    }

    getErrorMessage(form: FormGroup, field: string): string | null {
        const control = form.get(field);
        if (!control || !control.errors || !control.touched) return null;

        if (control.errors['required']) return 'Ce champ est obligatoire';
        if (control.errors['email']) return 'Email invalide';
        if (control.errors['pattern']) {
            if (field === 'telephone') return 'Le téléphone doit comporter exactement 8 chiffres';
            if (field === 'postalCode') return 'Le code postal doit comporter exactement 4 chiffres';
            return 'Format invalide';
        }
        if (control.errors['backend']) return control.errors['backend'];

        return 'Erreur de validation';
    }
}
