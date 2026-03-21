import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  InventoryService,
  InventoryAlertDTO,
  InventoryReportDTO,
  ProductSalesDTO,
  RestockResponseDTO
} from '../../../core/services/inventory.service';

@Component({
  selector: 'app-inventory-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inventory-dashboard.component.html',
  styles: `
    :host { display: block; padding-bottom: 3rem; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
    .page-header h1 { font-size: 1.5rem; font-weight: 700; margin: 0; }
    
    .dashboard-card { background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg); padding: 1.5rem; margin-bottom: 2rem; }
    .dashboard-card h3 { font-size: 1.1rem; font-weight: 700; margin-bottom: 1.5rem; display: flex; align-items: center; gap: 0.5rem; }
    
    .table-wrap { overflow-x: auto; margin: -1.5rem; margin-top: 0; }
    table { width: 100%; border-collapse: collapse; }
    th { text-align: left; padding: 1rem 1.5rem; border-bottom: 1px solid var(--color-border); color: var(--color-text-muted); font-size: 0.75rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; }
    td { padding: 1rem 1.5rem; border-bottom: 1px solid var(--color-border); vertical-align: middle; font-size: 0.95rem; }
    tbody tr:last-child td { border-bottom: none; }
    
    .text-danger { color: #dc3545; }
    .text-success { color: #198754; font-weight: 600; }
    .text-warning { color: #d39e00; font-weight: bold; }
    .text-muted { color: var(--color-text-muted); }
    
    .badge-id { font-size: 0.75rem; color: var(--color-text-muted); padding: 0.2rem 0.5rem; background: var(--color-surface-elevated); border-radius: 4px; border: 1px solid var(--color-border); margin-left: 0.5rem; }
    
    /* Report Styles */
    .report-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 2rem; }
    .report-section h4 { font-size: 0.85rem; font-weight: 700; margin-bottom: 1rem; color: var(--color-text); }
    .report-section h4.text-success { color: #198754; }
    .report-section h4.text-warning { color: #d39e00; }
    .report-section h4.text-danger { color: #dc3545; }
    
    .report-item { display: flex; justify-content: space-between; padding: 0.75rem 0; border-bottom: 1px solid var(--color-border); align-items: center; font-size: 0.95rem; }
    .report-item:last-child { border-bottom: none; }
    .report-item strong { font-size: 0.85rem; font-weight: 700; }
    
    /* Custom Modal Overlay */
    .custom-modal-backdrop { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1050; opacity: 0; pointer-events: none; transition: opacity 0.2s ease; }
    .custom-modal-backdrop.show { opacity: 1; pointer-events: auto; }
    .custom-modal { background: var(--color-surface); padding: 2rem; border-radius: var(--radius-lg); width: 100%; max-width: 450px; box-shadow: 0 10px 25px rgba(0,0,0,0.1); border: 1px solid var(--color-border); transform: translateY(-20px); transition: transform 0.2s ease; }
    .custom-modal-backdrop.show .custom-modal { transform: translateY(0); }
    .custom-modal h3 { margin-bottom: 0.5rem; font-size: 1.25rem; font-weight: 700; }
    .modal-actions { display: flex; gap: 0.75rem; justify-content: flex-end; margin-top: 1.5rem; }
  `
})
export class InventoryDashboardComponent implements OnInit {

  alerts: InventoryAlertDTO[] = [];
  report: InventoryReportDTO | null = null;
  topProducts: ProductSalesDTO[] = [];

  isLoadingAlerts = false;
  isLoadingReport = false;
  isLoadingTop = false;

  // Modal State
  showRestockModal = false;
  selectedProductId: number | null = null;
  restockQuantity: number | null = null;

  constructor(private inventoryService: InventoryService) { }

  ngOnInit(): void {
    this.refreshAll();
  }

  refreshAll(): void {
    this.loadAlerts();
    this.loadReport();
    this.loadTopProducts();
  }

  loadAlerts(): void {
    this.isLoadingAlerts = true;
    this.inventoryService.getAlerts().subscribe({
      next: (data) => {
        this.alerts = data;
        this.isLoadingAlerts = false;
      },
      error: (err) => {
        console.error('Failed to load inventory alerts', err);
        this.isLoadingAlerts = false;
      }
    });
  }

  loadReport(): void {
    this.isLoadingReport = true;
    this.inventoryService.getReport().subscribe({
      next: (data) => {
        this.report = data;
        this.isLoadingReport = false;
      },
      error: (err) => {
        console.error('Failed to load inventory report', err);
        this.isLoadingReport = false;
      }
    });
  }

  loadTopProducts(): void {
    this.isLoadingTop = true;
    this.inventoryService.getTopProducts(5).subscribe({
      next: (data) => {
        this.topProducts = data;
        this.isLoadingTop = false;
      },
      error: (err) => {
        console.error('Failed to load top products', err);
        this.isLoadingTop = false;
      }
    });
  }

  openRestockModal(productId: number): void {
    this.selectedProductId = productId;
    this.restockQuantity = null;
    this.showRestockModal = true;
  }

  closeRestockModal(): void {
    this.showRestockModal = false;
    this.selectedProductId = null;
    this.restockQuantity = null;
  }

  confirmRestock(): void {
    if (this.selectedProductId == null) return;

    if (this.restockQuantity != null) {
      if (this.restockQuantity <= 0) {
        alert('Veuillez entrer une quantité valide supérieure à 0.');
        return;
      }
      this.inventoryService.restockProduct(this.selectedProductId, this.restockQuantity).subscribe({
        next: (res: RestockResponseDTO) => {
          alert(`Successfully manually restocked ${res.productTitle}. Added ${res.addedQuantity} units. New Stock: ${res.newStock}`);
          this.refreshAll();
          this.closeRestockModal();
        },
        error: (err) => {
          console.error('Restock failed', err);
          alert('Failed to restock. Check console for details.');
        }
      });
    } else {
      this.inventoryService.restockProduct(this.selectedProductId).subscribe({
        next: (res: RestockResponseDTO) => {
          alert(`Successfully auto-restocked ${res.productTitle}. Added ${res.addedQuantity} units. New Stock: ${res.newStock}`);
          this.refreshAll();
          this.closeRestockModal();
        },
        error: (err) => {
          console.error('Restock failed', err);
          alert('Failed to restock. Check console for details.');
        }
      });
    }
  }
}
