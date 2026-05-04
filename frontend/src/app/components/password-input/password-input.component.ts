import { Component, input, model } from "@angular/core";

@Component({
    selector: 'app-password-input',
    template: `
    <div class="field">
        @if(label(); as label) {
            <label [for]="name()">{{label}}:</label>
        }
        <p class="control has-control-right">
            <input class="input" [name]="name()" [value]="value()" (input)="value.set(i.value)" [type]="unhide() ? 'text' : 'password'" #i>
            <span class="icon is-small is-left button" (click)="toggleUnhide()">{{unhide() ? 'H' : 'S'}}</span>
        </p>
    </div>
    `,
    styleUrl: './password-input.component.css'
})
export class PasswordInputComponent {
    name = input<string>();
    value = model('');
    unhide = model(false);
    label = input<string>();

    toggleUnhide() {
        this.unhide.update(old => !old);
    }

}