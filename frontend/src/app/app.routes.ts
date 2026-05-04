import { Routes } from '@angular/router';
import { PageNotFoundPageComponent } from './pages/page-not-found/page-not-found-page.component';
import { AccountPageComponent, AccountResolver } from './pages/account/account-page.component';
import { HomePageComponent } from './pages/home/home-page.component';
import { authenticationGuard } from './services/authentication.service';
import { AuthPageComponent, authPageGotoResolver } from './pages/auth/auth-page.component';
import { CoursePageComponent, coursePageResolver } from './pages/course/course.component';
import { CourseListPageComponent } from './pages/course-list/course-list.component';
import { admininistratorGuard, AdminPageComponent } from './pages/admin/admin.component';

export const routes: Routes = [
    {
        path: 'account/:id',
        component: AccountPageComponent,
        canActivate: [authenticationGuard(true)],
        resolve: {
            account: AccountResolver
        }
    },
    { path: 'courses', component: CourseListPageComponent, canActivate: [authenticationGuard(true), admininistratorGuard()] },
    {
        path: 'course/:id',
        component: CoursePageComponent,
        canActivate: [authenticationGuard(true)],
        resolve: {
            course: coursePageResolver
        }
    },
    {
        path: 'admin', component: AdminPageComponent, canActivate: [authenticationGuard(true)]
    },
    { path: '', component: HomePageComponent, pathMatch: 'full', canActivate: [authenticationGuard(true)] },
    {
        path: 'auth',
        component: AuthPageComponent,
        resolve: {
            goto: authPageGotoResolver
        }
    },

    { path: '404', component: PageNotFoundPageComponent, pathMatch: 'full' },
    { path: '**', redirectTo: '/404' }
];
