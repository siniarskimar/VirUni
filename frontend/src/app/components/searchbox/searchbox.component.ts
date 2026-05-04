import { Component, effect, input, model, output, signal } from "@angular/core";

@Component({
    selector: 'app-searchbox',
    template: `
    <form (submit)="$event.preventDefault(); enter.emit($event)">
        <input type="search"
            (input)="this.query.set(searchbox.value)"
            (blur)="this.blur.emit($event)"
            (focus)="this.focus.emit($event)"
            [value]="this.query()"
            [attr.placeholder]="placeholder()" #searchbox>
        <button type="submit" style="visibility: hidden;">search</button> <!-- accessibility -->
    </form>
    `
})
export class SearchboxComponent {
    query = model<string>();
    placeholder = input<string>();
    blur = output<Event>();
    focus = output<Event>();
    enter = output<Event>();
}