import {
    HttpRequest,
    HttpInterceptor,
    HttpHandler,
    HttpEvent,
} from '@angular/common/http';
import { Observable, EMPTY, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { SessionStorageService } from '../services/session-storage-service';

export class SimpleHttpInterceptor implements HttpInterceptor {
    constructor(private sessionService: SessionStorageService) {}

    intercept(
        req: HttpRequest<any>,
        next: HttpHandler
    ): Observable<HttpEvent<any>> {
        {
            const token = this.sessionService.sessionToken;
            const authReq = req.clone({
                headers: req.headers.set('Authorization', 'Bearer ' + token),
            });

            return next.handle(authReq).pipe(
                catchError((error, caught) => {
                    /**
                     * What to do in error case actually?
                     */
                    console.log('Intercepting HTTP request caused exception');
                    throw error;
                })
            );
            /*catch((error, caught) => {
                return Observable.throw(error);
            }) as any; */
        }
    }
}
