import {
  Component,
  OnDestroy,
  output,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';

@Component({
  selector: 'app-modal-wrapper',
  template: `
    <div class="modal-overlay" (click)="onBackdropClick($event)">
      <div class="modal-content">
        <div class="titlebar">
          <button class="close-btn" (click)="close.emit()">×</button>
        </div>
        <ng-template #content></ng-template>
      </div>
    </div>
  `,
  styleUrl: './modal-wrapper.component.css'
})
export class ModalWrapperComponent implements OnDestroy {
  @ViewChild('content', { read: ViewContainerRef, static: true })
  viewContainer!: ViewContainerRef;

  close = output<void>();

  onBackdropClick(event: MouseEvent) {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.close.emit();
    }
  }

  ngOnDestroy(): void {
    this.viewContainer.clear();
  }
}