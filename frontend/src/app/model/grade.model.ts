import { Account } from "@app/services/account.service";
import { Course } from "@app/services/course.service";

export default interface Grade {
    id: number;
    subject: Course;
    student: Account;
    teacher: Account;
    value: number;
    creation: string | Date;
}