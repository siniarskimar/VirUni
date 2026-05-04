import {
    EnvironmentInjector,
    Injectable,
    Injector,
    Type,
    inject,
} from '@angular/core';
import { ModalHostComponent } from './modal-host.component';
import { ModalWrapperComponent } from './modal-wrapper.component';
import { Observable, Subject } from 'rxjs';


export class ModalRef<T, R = any> {
    close!: (retval?: R) => void;
    afterClosed!: Observable<R | undefined>;

    constructor(close: (retval?: R) => void,
        afterClosed: Observable<R | undefined>) {
        this.close = close;
        this.afterClosed = afterClosed;
    }
}

@Injectable({
    providedIn: 'root'
})
export class ModalService {
    private readonly envInjector = inject(EnvironmentInjector);
    private hostComponent: ModalHostComponent | null = null;

    registerHost(host: ModalHostComponent) {
        this.hostComponent = host;
    }

    open<T, R = any>(component: Type<T>, data?: any): ModalRef<T, R> {
        if (!this.hostComponent) throw new Error('ModalHost not registered.');

        const container = this.hostComponent.viewContainerRef;
        const wrapperRef = container.createComponent(ModalWrapperComponent);

        const result$ = new Subject<R>();
        const close = (retval?: R) => {
            contentRef.destroy();
            wrapperRef.destroy();
            if (retval) result$.next(retval);
            result$.complete();
        };

        wrapperRef.instance.close.subscribe(() => close());

        const modalRef: ModalRef<T, R> = { close, afterClosed: result$.asObservable() };

        const injector = Injector.create({
            providers: [
                { provide: ModalRef, useValue: modalRef }
            ],
            parent: this.envInjector
        });

        // Create modal content inside wrapper
        const contentRef = wrapperRef.instance.viewContainer.createComponent(component, { injector });

        for (const [key, value] of Object.entries(data || {})) {
            contentRef.setInput(key, value);
        }

        return modalRef;
    }
}