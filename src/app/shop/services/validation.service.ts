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

        if (control.errors['required']) return 'This field is required';
        if (control.errors['email']) return 'Invalid email';
        if (control.errors['pattern']) {
            if (field === 'telephone') return 'Phone number must be exactly 8 digits';
            if (field === 'postalCode') return 'Postal code must be exactly 4 digits';
            return 'Invalid format';
        }
        if (control.errors['backend']) return control.errors['backend'];

        return 'Validation error';
    }
}
