import { Component, OnInit } from '@angular/core';
import { EventClient } from 'src/app/rest/event-client';
import { ActivatedRoute, Router } from '@angular/router';
import { Event } from 'src/app/models/event';
import { NgForm } from '@angular/forms';
import { EventType } from 'src/app/models/enum/eventType';
import { Customer } from 'src/app/models/customer';
import { Room } from 'src/app/models/enum/room';
import { ConditionalExpr } from '@angular/compiler';

@Component({
    selector: 'app-cancel-event',
    templateUrl: './cancel-event.component.html',
    styleUrls: ['./cancel-event.component.scss'],
})
export class CancelEventComponent implements OnInit {
    private event: Event = new Event();

    private customerToRemove: Customer;

    private title: string;
    private textBox: string;
    private valid: boolean;
    private successMsg: string;
    private errorMsg: string;

    private preCountOfCustomers: number;

    btnText: string;

    private signOff: boolean;

    constructor(
        private eventClient: EventClient,
        private route: ActivatedRoute,
        private router: Router
    ) { }

    ngOnInit() {
        const id: number = this.route.snapshot.queryParams.id;
        if (id === undefined) {
            this.title = 'Dieser URL ist nicht gültig';
            this.textBox = 'Sie hätten dieser Seite garnicht erreichen sollen';
            this.valid = false;
        } else {
            console.log('Getting event with id ' + id);
            this.eventClient.getEventById(id).subscribe(
                (data: Event) => {
                    console.log('Got event with id ' + data.id);
                    this.event = data;

                    // SIGN OFF COURSE

                    const eType = data.eventType as EventType;

                    if (eType === EventType.Course) {
                        this.btnText = 'Abmelden';
                        const emailId = Number(
                            this.route.snapshot.queryParams.emailId
                        );
                        console.log(data.customerDtos);

                        this.signOff = true;
                        this.preCountOfCustomers = data.customerDtos.length;
                        for (const customer of data.customerDtos) {
                            console.log(emailId + ' und ' + customer.emailId);
                            if (customer.emailId === emailId) {
                                this.title =
                                    'Hallo ' +
                                    customer.firstName +
                                    ' ' +
                                    customer.lastName +
                                    '!';
                                this.customerToRemove = customer;
                            }
                        }
                        this.textBox =
                            'Wollen Sie sich wirklich von ' +
                            data.name +
                            ' abmelden?';
                    } else {
                        this.btnText = 'Stornieren';
                        this.signOff = false;
                        this.title =
                            'Hallo ' +
                            data.customerDtos[0].firstName +
                            ' ' +
                            data.customerDtos[0].lastName +
                            '!';
                        this.textBox =
                            'Wollen Sie wirklich ' + data.name + ' stornieren?';
                    }
                    this.valid = true;
                },
                (error) => {
                    this.title = 'Fehler 404';
                    this.textBox = 'UPS, da ist was schief gelaufen';
                    this.valid = false;
                    console.log(error);
                }
            );
        }

        if (this.event === null || this.event === undefined) {
            this.title = "Fehler 404";
            this.textBox = "Dieser Ereignis existiert nicht in der Datenbank"
            this.valid = false;
        } else {
            console.log('Got event with id ' + this.event.id);
            this.title = "Hallo!";
            this.textBox = "Wollen sie wirklich stornieren?";
            this.valid = true;
        }
    }

    public cancelEvent(form: NgForm): void {
        const id: number = this.route.snapshot.queryParams.id;

        if (this.signOff) {
            this.event.customerDtos = [this.customerToRemove];
            this.eventClient.updateCustomer(this.event).subscribe(
                (data: Event) => {
                    if (
                        data.customerDtos.length ===
                        this.preCountOfCustomers - 1
                    ) {
                        this.successMsg = 'Sie wurden erfolgreich abgemeldet';
                    } else {
                        console.log(data);
                        this.successMsg = 'Etwas ist schief gelaufen';
                    }
                },
                (error: Error) => {
                    console.log(error);
                    this.errorMsg = 'Etwas ist schief gelaufen';
                }
            );
            this.valid = false;
        } else {
            this.eventClient.cancelEvent(id).subscribe(
                () => {
                    this.successMsg = 'Ihr Event wurde erfolgreich storniert';
                },
                (error: Error) => {
                    console.log(error.message);
                    this.errorMsg =
                        'Ihr Event konnte nicht storniert werden ' +
                        error.message;
                }
            );
        }
    }
}
