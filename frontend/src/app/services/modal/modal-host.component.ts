import {
    Component,
    ViewChild,
    ViewContainerRef,
} from '@angular/core';

@Component({
    selector: 'app-modal-host',
    template: `<ng-template #modalContainer></ng-template>`
})
export class ModalHostComponent {
    @ViewChild('modalContainer', { read: ViewContainerRef, static: true })
    viewContainerRef!: ViewContainerRef;
}