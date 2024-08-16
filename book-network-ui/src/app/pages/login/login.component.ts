import { Component } from '@angular/core';
import {AuthenticationRequest} from "../../services/models/authentication-request";
import {NgForOf, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {AuthenticationService} from "../../services/services/authentication.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    NgIf,
    FormsModule,
    NgForOf,

  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {

  authRequest: AuthenticationRequest = {email:'', password:''}
  errorMessage :string[] = [];
  constructor(private authService: AuthenticationService,private router: Router) {

  }

  login() {
    this.errorMessage = [];
    this.authService.authenticate({
      body: this.authRequest
    }).subscribe({
      next:(res):void => {
        //save the token
        this.router.navigate(['books'])
      },
      error:(err):void =>{
        console.log(err);
      }
    });

  }

  register() {
    this.router.navigate(['register'])
  }

}
