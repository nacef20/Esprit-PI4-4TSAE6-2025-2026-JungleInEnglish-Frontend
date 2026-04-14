import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  InventoryService,
  InventoryAlertDTO,
  InventoryReportDTO,
  ProductSalesDTO,
  RestockResponseDTO
} from '../../../services/inventory.service';
import { MessageService } from '../../../services/message.service';

@Component({
  selector: 'app-inventory-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inventory-dashboard.component.html',
  styles: `
    :host { 
      display: block; 
      padding: 0 1rem 3rem 1rem; 
      font-family: 'Inter', sans-serif; 
    }
    
    .admin-container {
      max-width: 1400px;
      margin: 0 auto;
    }

    .admin-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      margin-bottom: 2rem;
      border-bottom: 1px solid rgba(0,0,0,0.05);
      padding-bottom: 1.5rem;
      padding-top: 1rem;
    }

    .admin-header h1 {
      font-size: 2rem;
      font-weight: 800;
      background: linear-gradient(135deg, #0ea37a 0%, #065f46 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      margin-bottom: 0.25rem;
    }
    
    .emoji {
      -webkit-background-clip: unset;
      -webkit-text-fill-color: initial;
      background: none;
    }

    .admin-header p {
      color: #64748b;
      margin: 0;
      font-size: 1.05rem;
    }

    .admin-stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 1.5rem;
      margin-bottom: 2rem;
    }

    .admin-stat-card {
      background: rgba(255, 255, 255, 0.9);
      backdrop-filter: blur(10px);
      border: 1px solid rgba(226, 232, 240, 0.8);
      border-radius: 16px;
      padding: 1.5rem;
      display: flex;
      align-items: center;
      gap: 1.25rem;
      box-shadow: 0 4px 15px rgba(0,0,0,0.02);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }

    .admin-stat-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 12px 25px rgba(0,0,0,0.08);
      border-color: #cbd5e1;
    }

    .admin-stat-card .icon {
      width: 56px;
      height: 56px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.5rem;
    }

    .admin-stat-card .content h3 {
      font-size: 1.75rem;
      font-weight: 800;
      color: #1e293b;
      margin: 0 0 0.25rem 0;
      line-height: 1;
    }

    .admin-stat-card .content p {
      color: #64748b;
      margin: 0;
      font-weight: 500;
      font-size: 0.95rem;
    }

    .admin-card {
      background: #ffffff;
      border-radius: 16px;
      border: 1px solid #e2e8f0;
      box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
      padding: 1.5rem;
      margin-bottom: 2rem;
    }

    .admin-card h3 {
      margin-top: 0;
      margin-bottom: 1.25rem;
      color: #1e293b;
      font-size: 1.2rem;
      font-weight: 700;
      padding-bottom: 0.75rem;
      border-bottom: 1px solid #f1f5f9;
    }

    .admin-table-wrap {
      overflow-x: auto;
      margin: 0 -1.5rem -1.5rem -1.5rem;
    }

    table {
      width: 100%;
      border-collapse: collapse;
      text-align: left;
    }

    th, td {
      padding: 1rem 1.5rem;
      border-bottom: 1px solid #f1f5f9;
      vertical-align: middle;
    }

    th {
      font-size: 0.8rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      color: #64748b;
      font-weight: 700;
      background: #f8fafc;
    }

    tr:last-child td { border-bottom: none; }
    
    tr:hover td { background: #f8fafc; }

    .admin-btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      padding: 0.6rem 1.2rem;
      font-size: 0.95rem;
      font-weight: 600;
      border-radius: 10px;
      border: none;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .admin-btn--primary {
      background: linear-gradient(135deg, #0ea37a 0%, #065f46 100%);
      color: white;
      box-shadow: 0 4px 10px rgba(14, 163, 122, 0.25);
    }

    .admin-btn--primary:hover {
      box-shadow: 0 6px 15px rgba(14, 163, 122, 0.4);
      transform: translateY(-1px);
    }

    .admin-btn--secondary {
      background: #f1f5f9;
      color: #475569;
      border: 1px solid #cbd5e1;
    }

    .admin-btn--secondary:hover {
      background: #e2e8f0;
      color: #1e293b;
    }

    .admin-btn--sm {
      padding: 0.4rem 0.8rem;
      font-size: 0.85rem;
      border-radius: 6px;
    }

    .admin-badge {
      display: inline-block;
      padding: 0.25rem 0.65rem;
      border-radius: 20px;
      font-size: 0.8rem;
      font-weight: 700;
      text-transform: uppercase;
    }

    .admin-badge--primary { background: #e0f2fe; color: #0369a1; }
    .admin-badge--warning { background: #fef3c7; color: #b45309; }

    .admin-loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 3rem;
      color: #64748b;
    }

    .spinner {
      width: 40px;
      height: 40px;
      border: 4px solid rgba(14, 163, 122, 0.2);
      border-top-color: #0ea37a;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin-bottom: 1rem;
    }

    @keyframes spin { to { transform: rotate(360deg); } }

    /* Modals */
    .custom-modal-backdrop { 
      position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; 
      background: rgba(0,0,0,0.4); backdrop-filter: blur(4px); 
      display: flex; align-items: center; justify-content: center; 
      z-index: 1050; opacity: 0; pointer-events: none; transition: opacity 0.3s ease; 
    }
    .custom-modal-backdrop.show { opacity: 1; pointer-events: auto; }
    
    .custom-modal { 
      background: white; padding: 2rem; border-radius: 16px; 
      width: 100%; max-width: 480px; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04); 
      border: 1px solid #f1f5f9; transform: scale(0.9) translateY(20px); transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); 
    }
    .custom-modal-backdrop.show .custom-modal { transform: scale(1) translateY(0); }
    
    .admin-input {
      width: 100%; padding: 0.75rem 1rem; border: 1px solid #e2e8f0; border-radius: 12px; font-weight: 500;
      transition: all 0.2s; font-family: inherit;
    }
    .admin-input:focus { outline: none; border-color: #0ea37a; box-shadow: 0 0 0 3px rgba(14, 163, 122, 0.1); }
    
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

  constructor(
    private inventoryService: InventoryService,
    private msg: MessageService
  ) { }

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
        this.msg.error('Please enter a valid quantity greater than 0.');
        return;
      }
      this.inventoryService.restockProduct(this.selectedProductId, this.restockQuantity).subscribe({
        next: (res: RestockResponseDTO) => {
          this.msg.success(`Manual restock successful for ${res.productTitle}. ${res.addedQuantity} added. New stock: ${res.newStock}`);
          this.refreshAll();
          this.closeRestockModal();
        },
        error: (err) => {
          console.error('Restock failed', err);
          this.msg.error('Restock failed. Check the console for more details.');
        }
      });
    } else {
      this.inventoryService.restockProduct(this.selectedProductId).subscribe({
        next: (res: RestockResponseDTO) => {
          this.msg.success(`Automatic restock successful for ${res.productTitle}. ${res.addedQuantity} added. New stock: ${res.newStock}`);
          this.refreshAll();
          this.closeRestockModal();
        },
        error: (err) => {
          console.error('Restock failed', err);
          this.msg.error('Restock failed. Check the console for more details.');
        }
      });
    }
  }
}
