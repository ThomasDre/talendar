import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { TrainerClient } from 'src/app/rest/trainer-client';
import { Trainer } from 'src/app/models/trainer';
import { HttpResponse } from '@angular/common/http';

@Component({
    selector: 'app-trainer',
    templateUrl: './trainer.component.html',
    styleUrls: ['./trainer.component.scss'],
})
export class TrainerComponent implements OnInit {
    private title = 'Betreuer erstellen';
    private trainer: Trainer = new Trainer();
    private errorMsg: string;
    private successMsg: string;

    constructor(private trainerClient: TrainerClient) {}

    ngOnInit() {}

    public postTrainer(form: NgForm): void {
        console.log('Pass Form Data To Rest Client');
        this.trainerClient.postNewTrainer(this.trainer).subscribe(
            (data: Trainer) => {
                console.log(data);
                this.successMsg = 'Der Betreuer wurde erfolgreich gespeichert';
            },
            (error) => {
                console.log(error);
                this.errorMsg = 'Der Betreuer konnte nicht angelegt werden!';
            }
        );
    }

    public isCompleted(): boolean {
        if (
            this.trainer.firstName === undefined ||
            this.trainer.firstName === ''
        ) {
            return false;
        }
        if (
            this.trainer.lastName === undefined ||
            this.trainer.lastName === ''
        ) {
            return false;
        }
        if (this.trainer.birthday === undefined) {
            return false;
        }
        if (this.trainer.email === undefined || this.trainer.email === '') {
            return false;
        }
        if (this.trainer.phone === undefined || this.trainer.phone === '') {
            return false;
        }
        return true;
    }

    public clearInfoMsg(): void {
        this.errorMsg = undefined;
        this.successMsg = undefined;
    }
}
