import { Component, OnInit } from '@angular/core';
import { registerLocaleData, DatePipe } from '@angular/common';
import localeDe from '@angular/common/locales/de-AT';

import { CalendarDateFormatter, CalendarView } from 'angular-calendar';

import { CustomDateFormatter } from './CustomDateFormatter';
import { MetaEvent } from './MetaEvent';
import { Event } from '../../models/event';
import { BREAKPOINTS } from 'src/app/utils/Breakpoints';
import { EventClient } from 'src/app/rest/event-client';
import { EventImportService } from 'src/app/services/event-import.service';
import { Trainer } from 'src/app/models/trainer';
import { TrainerClient } from 'src/app/rest/trainer-client';

/**
 * In order to display week days in German the locale data
 * needs to be imported and registered, since the calender
 * uses Angular's i18n API.
 */
registerLocaleData(localeDe);

@Component({
    selector: 'app-calendar',
    templateUrl: './calendar.component.html',
    styleUrls: ['./calendar.component.scss'],
    providers: [
        { provide: CalendarDateFormatter, useClass: CustomDateFormatter },
    ],
})
export class CalendarComponent implements OnInit {
    calendarView = CalendarView;
    dayEndHour = 20;
    dayEndMinute = 0;
    daysInWeek: number | null = null;
    dayStartHour = 8;
    dayStartMinute = 0;
    // list of all loaded events
    allEvents: MetaEvent[] = [];
    // list of all events relative to the applied filters
    filteredEvents: MetaEvent[] = [];
    hourSegments = 4;
    locale = 'de-AT';
    precision = 'minutes';
    view = CalendarView.Week;
    viewDate = new Date();
    weekStartsOn = 1;
    navButtonLabel: { prev: string; next: string };

    // filter specific content

    // filterable
    rooms: string[] = ['Grün', 'Orange', 'Erdgeschoss', 'Reset'];
    eventTypes: string[] = ['Kurs', 'Beratung', 'Geburstag', 'Miete', 'Reset'];
    trainerList: string[] = [];
    trainers: Trainer[] = [];

    // filter selections
    roomSelection: string;
    eventTypeSelection: string;
    trainerSelection: string;
    minAgeFilter: number;
    maxAgeFilter: number;

    constructor(
        private eventClient: EventClient,
        private trainerClient: TrainerClient,
        private eventImport: EventImportService
    ) {
        if (screen.width < BREAKPOINTS.medium) {
            this.daysInWeek = 3;
        } else if (screen.width < BREAKPOINTS.small) {
            this.daysInWeek = 1;
        }
        this.updateNavButtonLabel();
    }

    ngOnInit() {
        this.eventClient.getAllEvents().subscribe((data: Event[]) => {
            this.allEvents = this.eventImport.mapEventsToCalendar(data);
            this.filteredEvents = this.allEvents;
        });

        this.trainerClient.getAll().subscribe((data: Trainer[]) => {
            this.trainers = data;

            for (const trainer of this.trainers) {
                const name = trainer.firstName + ' ' + trainer.lastName;
                this.trainerList.push(name);
            }

            this.trainerList.push('Reset');
        });
    }

    updateNavButtonLabel() {
        console.log('label getter');
        const label = { prev: 'Vorherige', next: 'Nächste' };

        if (this.view === this.calendarView.Month || this.daysInWeek === 1) {
            label.prev = 'Vorheriger';
            label.next = 'Nächster';
        }
        this.navButtonLabel = label;
    }

    toggleView(view: CalendarView, daysInWeek: null | number) {
        this.view = view;
        this.daysInWeek = daysInWeek;
        this.updateNavButtonLabel();
    }

    setRoomFilter(filter: string): void {
        if (filter === 'Reset') {
            this.roomSelection = undefined;
        } else {
            this.roomSelection = filter;
        }
        this.updateView();
    }

    setTrainerFilter(filter: string): void {
        if (filter === 'Reset') {
            this.trainerSelection = undefined;
        } else {
            this.trainerSelection = filter;
        }
        this.updateView();
    }

    setEventTypeFilter(filter: string): void {
        if (filter === 'Reset') {
            this.eventTypeSelection = undefined;
        } else {
            this.eventTypeSelection = filter;
        }
        this.updateView();
    }

    public updateView(): void {
        console.log('ASAS');
        this.filteredEvents = this.allEvents;

        this.filteredEvents = this.filteredEvents.filter((event: MetaEvent) => {
            // vars are true if filter for this context was set in GUI
            const hasRoomFilter: boolean = this.roomSelection !== undefined;
            const hasTrainerFilter: boolean =
                this.trainerSelection !== undefined;
            const hasTypeFilter: boolean =
                this.eventTypeSelection !== undefined;

            // check if given filters satisfy given event properties
            // iff filter doesnt match (ret false) remove this elem from array

            if (hasRoomFilter) {
                const transformedQuery: string = this.mapToEnumValue(
                    this.roomSelection
                );

                if (
                    transformedQuery !== event.event.roomUses[0].room.toString()
                ) {
                    return false;
                }
            }

            // this filter is not apllicable to events of type 'rent'
            // as rents have no assigned trainer (null)
            if (hasTrainerFilter) {
                if (
                    event.event.trainer === null ||
                    event.event.trainer === undefined
                ) {
                    return false;
                }

                const trainerNameOfEvent =
                    event.event.trainer.firstName +
                    ' ' +
                    event.event.trainer.lastName;

                if (this.trainerSelection !== trainerNameOfEvent) {
                    return false;
                }
            }

            if (hasTypeFilter) {
                const transformedQuery: string = this.mapToEnumValue(
                    this.eventTypeSelection
                );

                if (transformedQuery !== event.event.eventType.toString()) {
                    return false;
                }
            }

            if (hasTypeFilter && this.eventTypeSelection === 'Kurs') {
                if (
                    this.minAgeFilter !== null &&
                    event.event.minAge < this.minAgeFilter
                ) {
                    return false;
                }

                if (
                    this.maxAgeFilter !== null &&
                    event.event.maxAge > this.maxAgeFilter
                ) {
                    return false;
                }
            }

            return true;
        });
    }

    /**
     * This method maps the german search params that can be selceted (e.g. 'Erdgeschoss' when
     * searching for a room, or 'Beratung' wehn searching for a specific event type) to the
     * english based enum strings. This is done in order to comapare the given params
     * programatically with these fixed values.
     *
     * Note that this is not the smoothest solution (i know): changing the enums to german
     * can be considered too (among other possible solutions)
     *
     *
     * @param name the given query string (german) that should be mapped to the equivalent
     * englisch enum string
     */
    private mapToEnumValue(name: string): string {
        if (name === 'Grün') {
            return 'Green';
        }
        if (name === 'Orange') {
            return 'Orange';
        }
        if (name === 'Erdgeschoss') {
            return 'GroundFloor';
        }
        if (name === 'Kurs') {
            return 'Course';
        }
        if (name === 'Beratung') {
            return 'Consultation';
        }
        if (name === 'Geburstag') {
            return 'Birthday';
        }
        if (name === 'Miete') {
            return 'Rent';
        }

        return null;
    }
}
