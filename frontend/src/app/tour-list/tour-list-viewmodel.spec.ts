import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { TourListViewmodel } from './tour-list-viewmodel';
import { TourService } from '../services/tour.service';
import { Tour, TransportType } from '../models/tour.model';

function makeTour(id: number, name: string): Tour {
  return {
    id,
    name,
    description: 'desc',
    fromLocation: 'A',
    toLocation: 'B',
    transportType: TransportType.Walk,
    distance: 1,
    estimatedTime: '00:10',
  };
}

describe('TourListViewmodel', () => {
  let vm: TourListViewmodel;
  let serviceMock: any;

  beforeEach(() => {
    const toursSignal = signal<Tour[]>([makeTour(1, 'Vienna'), makeTour(2, 'Graz')]);
    serviceMock = {
      tours: toursSignal.asReadonly(),
      loadAll: vi.fn(),
      search: vi.fn(),
      delete: vi.fn(),
      add: vi.fn(),
      update: vi.fn(),
      exportTours: vi.fn(),
      importTours: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        TourListViewmodel,
        { provide: TourService, useValue: serviceMock },
      ],
    });

    vm = TestBed.inject(TourListViewmodel);
  });

  it('should be created', () => {
    expect(vm).toBeTruthy();
  });

  it('loads all tours on creation', () => {
    expect(serviceMock.loadAll).toHaveBeenCalled();
  });

  it('exposes the tours signal from the service', () => {
    expect(vm.tours().length).toBe(2);
  });

  it('selectedTour is null when nothing is selected', () => {
    expect(vm.selectedTour()).toBeNull();
  });

  it('selectTour updates the selected id and computed tour', () => {
    vm.selectTour(2);
    expect(vm.selectedTourId()).toBe(2);
    expect(vm.selectedTour()?.name).toBe('Graz');
  });

  it('deleteTour delegates to the service and clears a matching selection', () => {
    vm.selectTour(1);
    vm.deleteTour(1);
    expect(serviceMock.delete).toHaveBeenCalledWith(1);
    expect(vm.selectedTourId()).toBeNull();
  });

  it('search with an empty term reloads all tours', () => {
    vm.search('');
    expect(serviceMock.loadAll).toHaveBeenCalled();
  });

  it('search with a term delegates to service.search and stores the term', () => {
    vm.search('vienna');
    expect(serviceMock.search).toHaveBeenCalledWith('vienna');
    expect(vm.currentSearchTerm()).toBe('vienna');
  });

  it('addTour delegates to the service', () => {
    const t = makeTour(3, 'Linz');
    vm.addTour(t);
    expect(serviceMock.add).toHaveBeenCalled();
  });

  it('updateTour delegates to the service', () => {
    const t = makeTour(1, 'Vienna v2');
    vm.updateTour(t);
    expect(serviceMock.update).toHaveBeenCalledWith(t);
  });

  it('exportTours delegates to the service', () => {
    vm.exportTours();
    expect(serviceMock.exportTours).toHaveBeenCalled();
  });

  it('importTours forwards the file to the service', () => {
    const file = new File(['[]'], 'tours.json', { type: 'application/json' });
    vm.importTours(file);
    expect(serviceMock.importTours).toHaveBeenCalledWith(file);
  });
});