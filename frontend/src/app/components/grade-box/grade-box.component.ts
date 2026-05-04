import { Component, computed, input, output } from "@angular/core";


@Component({
    selector: 'app-grade-box',
    template: `
        <div class="grade-box tag has-text-dark is-large">{{value() ? value() : '?'}}</div>
    `,
    styleUrl: './grade-box.component.css'
})
export class GradeBoxComponent {
    id = input<number>();
    value = input<number>();
}