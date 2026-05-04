import { Component, input, model, output } from "@angular/core";

@Component({
    selector: "app-editable-shorttext",
    templateUrl: './editable-shorttext.component.html',
    styleUrl: './editable-shorttext.component.css'
})
export class EditableShorttextComponent {
    text = input<string>();
    editConfimed = output<string>();
    readOnly = input<boolean>(true);

    editing: boolean = false;
}